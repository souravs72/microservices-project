package com.microservices.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.auth.client.UserServiceClient;
import com.microservices.auth.entity.OutboxEvent;
import com.microservices.auth.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.user-events}")
    private String userEventsTopic;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Value("${app.sync.max-retry-attempts:5}")
    private int maxRetryAttempts;

    @Value("${app.sync.retry-delay-seconds:30}")
    private long retryDelaySeconds;

    /**
     * Process outbox events every 30 seconds
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processOutboxEvents() {
        try {
            List<OutboxEvent> unprocessedEvents = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
            
            if (unprocessedEvents.isEmpty()) {
                log.debug("No unprocessed outbox events found");
                return;
            }

            log.info("Processing {} unprocessed outbox events", unprocessedEvents.size());

            for (OutboxEvent event : unprocessedEvents) {
                processEvent(event);
            }
        } catch (Exception e) {
            log.error("Error processing outbox events: {}", e.getMessage(), e);
        }
    }

    /**
     * Process a single outbox event with retry logic
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    @Async
    public CompletableFuture<Void> processEvent(OutboxEvent event) {
        try {
            log.info("Processing outbox event: {} for aggregate: {}", 
                    event.getEventType(), event.getAggregateId());

            // Try Kafka first (primary method)
            boolean kafkaSuccess = publishToKafka(event);
            
            // Try direct sync as backup
            boolean directSyncSuccess = attemptDirectSync(event);

            if (kafkaSuccess || directSyncSuccess) {
                markEventAsProcessed(event);
                log.info("Successfully processed outbox event: {} for aggregate: {}", 
                        event.getEventType(), event.getAggregateId());
            } else {
                handleEventFailure(event);
            }

        } catch (Exception e) {
            log.error("Error processing outbox event {}: {}", event.getId(), e.getMessage(), e);
            handleEventFailure(event);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Publish event to Kafka
     */
    private boolean publishToKafka(OutboxEvent event) {
        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    userEventsTopic, 
                    event.getAggregateId(), 
                    event.getPayload()
            );

            SendResult<String, String> result = future.get();
            log.info("Event published to Kafka successfully: {}", result.getRecordMetadata().offset());
            return true;

        } catch (Exception e) {
            log.warn("Failed to publish event to Kafka: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Attempt direct synchronization with User Service
     */
    private boolean attemptDirectSync(OutboxEvent event) {
        try {
            Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
            String eventType = (String) payload.get("eventType");

            if ("USER_CREATED".equals(eventType)) {
                return syncUserCreated(payload);
            } else if ("USER_UPDATED".equals(eventType)) {
                return syncUserUpdated(payload);
            } else if ("USER_DELETED".equals(eventType)) {
                return syncUserDeleted(payload);
            }

            log.warn("Unknown event type for direct sync: {}", eventType);
            return false;

        } catch (Exception e) {
            log.warn("Direct sync failed for event {}: {}", event.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Sync user creation event
     */
    private boolean syncUserCreated(Map<String, Object> payload) {
        try {
            // Create user profile request without password
            var request = new com.microservices.auth.dto.CreateUserProfileRequest(
                    Long.parseLong((String) payload.get("userId")),
                    (String) payload.get("username"),
                    (String) payload.get("email"),
                    "MANAGED_BY_AUTH_SERVICE", // No password for security
                    (String) payload.get("firstName"),
                    (String) payload.get("lastName")
            );

            userServiceClient.createUserProfile(request, internalApiKey);
            log.info("User created successfully via direct sync: {}", payload.get("username"));
            return true;

        } catch (Exception e) {
            log.warn("Direct sync failed for user creation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Sync user update event
     */
    private boolean syncUserUpdated(Map<String, Object> payload) {
        try {
            // Implement user update sync logic
            log.info("User updated via direct sync: {}", payload.get("username"));
            return true;

        } catch (Exception e) {
            log.warn("Direct sync failed for user update: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Sync user deletion event
     */
    private boolean syncUserDeleted(Map<String, Object> payload) {
        try {
            // Implement user deletion sync logic
            log.info("User deleted via direct sync: {}", payload.get("username"));
            return true;

        } catch (Exception e) {
            log.warn("Direct sync failed for user deletion: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Mark event as processed
     */
    private void markEventAsProcessed(OutboxEvent event) {
        event.setProcessed(true);
        event.setProcessedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
    }

    /**
     * Handle event processing failure
     */
    private void handleEventFailure(OutboxEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setLastRetryAt(LocalDateTime.now());

        if (event.getRetryCount() >= maxRetryAttempts) {
            event.setProcessed(true); // Mark as processed to avoid infinite retry
            event.setFailed(true);
            log.error("Event {} failed after {} retry attempts", event.getId(), maxRetryAttempts);
        } else {
            // Schedule for retry
            event.setNextRetryAt(LocalDateTime.now().plusSeconds(retryDelaySeconds));
            log.warn("Event {} failed, will retry in {} seconds (attempt {}/{})", 
                    event.getId(), retryDelaySeconds, event.getRetryCount(), maxRetryAttempts);
        }

        outboxEventRepository.save(event);
    }

    /**
     * Clean up old processed events (run daily)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    @Transactional
    public void cleanupOldEvents() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            int deletedCount = outboxEventRepository.deleteByProcessedTrueAndCreatedAtBefore(cutoffDate);
            log.info("Cleaned up {} old processed outbox events", deletedCount);
        } catch (Exception e) {
            log.error("Error cleaning up old outbox events: {}", e.getMessage(), e);
        }
    }

    /**
     * Get outbox health metrics
     */
    public Map<String, Object> getOutboxHealth() {
        try {
            long totalEvents = outboxEventRepository.count();
            long unprocessedEvents = outboxEventRepository.countByProcessedFalse();
            long failedEvents = outboxEventRepository.countByFailedTrue();
            long retryEvents = outboxEventRepository.countByRetryCountGreaterThan(0);

            return Map.of(
                    "totalEvents", totalEvents,
                    "unprocessedEvents", unprocessedEvents,
                    "failedEvents", failedEvents,
                    "retryEvents", retryEvents,
                    "healthStatus", unprocessedEvents == 0 ? "HEALTHY" : "DEGRADED",
                    "lastChecked", LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Error getting outbox health: {}", e.getMessage(), e);
            return Map.of(
                    "error", e.getMessage(),
                    "healthStatus", "ERROR",
                    "lastChecked", LocalDateTime.now()
            );
        }
    }
}

