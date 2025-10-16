package com.microservices.auth.scheduler;

import com.microservices.auth.entity.OutboxEvent;
import com.microservices.auth.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "outbox.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final int MAX_RETRY_COUNT = 3;

    @Scheduled(fixedDelayString = "${outbox.scheduler.fixed-delay:5000}")
    @Transactional
    public void processOutboxEvents() {
        int batchSize = 100;
        List<OutboxEvent> events = outboxEventRepository.findUnprocessedEvents(
                PageRequest.of(0, batchSize)
        );

        if (events.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                // Set correlation ID from event
                if (event.getCorrelationId() != null) {
                    MDC.put("correlationId", event.getCorrelationId());
                }

                processEvent(event);
                markAsProcessed(event);

                log.debug("Successfully processed outbox event: {}", event.getId());
            } catch (Exception e) {
                handleProcessingError(event, e);
            } finally {
                MDC.remove("correlationId");
            }
        }

        log.info("Completed processing {} outbox events", events.size());
    }

    private void processEvent(OutboxEvent event) {
        switch (event.getEventType()) {
            case "USER_CREATED":
                kafkaTemplate.send(USER_EVENTS_TOPIC, event.getAggregateId(), event.getPayload());
                break;
            case "USER_UPDATED":
                kafkaTemplate.send(USER_EVENTS_TOPIC, event.getAggregateId(), event.getPayload());
                break;
            default:
                log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void markAsProcessed(OutboxEvent event) {
        event.setProcessed(true);
        event.setProcessedAt(LocalDateTime.now());
        event.setErrorMessage(null);
        outboxEventRepository.save(event);
    }

    private void handleProcessingError(OutboxEvent event, Exception e) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setErrorMessage(e.getMessage());

        if (event.getRetryCount() >= MAX_RETRY_COUNT) {
            log.error("Max retry count reached for outbox event: {}. Moving to DLQ", event.getId(), e);
            // In production, move to Dead Letter Queue
            event.setProcessed(true);
            event.setProcessedAt(LocalDateTime.now());
        }

        outboxEventRepository.save(event);
        log.error("Error processing outbox event: {}", event.getId(), e);
    }

    @Scheduled(cron = "0 0 4 * * ?")  // Run daily at 4 AM
    @Transactional
    public void cleanupOldProcessedEvents() {
        log.info("Starting cleanup of old processed outbox events");
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        outboxEventRepository.deleteProcessedEventsOlderThan(cutoffDate);
        log.info("Completed cleanup of old processed outbox events");
    }
}