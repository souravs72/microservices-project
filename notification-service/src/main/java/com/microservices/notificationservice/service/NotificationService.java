package com.microservices.notificationservice.service;

import com.microservices.notificationservice.dto.OrderNotificationEvent;
import com.microservices.notificationservice.dto.UserCreatedEvent;
import com.microservices.notificationservice.entity.NotificationHistory;
import com.microservices.notificationservice.repository.NotificationHistoryRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class NotificationService {

    private final NotificationHistoryRepository historyRepository;
    private final EmailService emailService;
    private final IdempotencyService idempotencyService;
    private final Counter notificationProcessedCounter;
    private final Counter notificationSuccessCounter;
    private final Counter notificationFailureCounter;
    private final Timer notificationProcessingTimer;

    public NotificationService(NotificationHistoryRepository historyRepository,
                              EmailService emailService,
                              IdempotencyService idempotencyService,
                              @Qualifier("notificationProcessedCounter") Counter notificationProcessedCounter,
                              @Qualifier("notificationSuccessCounter") Counter notificationSuccessCounter,
                              @Qualifier("notificationFailureCounter") Counter notificationFailureCounter,
                              @Qualifier("notificationProcessingTimer") Timer notificationProcessingTimer) {
        this.historyRepository = historyRepository;
        this.emailService = emailService;
        this.idempotencyService = idempotencyService;
        this.notificationProcessedCounter = notificationProcessedCounter;
        this.notificationSuccessCounter = notificationSuccessCounter;
        this.notificationFailureCounter = notificationFailureCounter;
        this.notificationProcessingTimer = notificationProcessingTimer;
    }

    @Transactional
    public CompletableFuture<Void> processUserCreatedEvent(UserCreatedEvent event, String eventId) {
        Timer.Sample sample = Timer.start();
        notificationProcessedCounter.increment();
        
        try {
            // Check idempotency
            if (idempotencyService.isProcessed(eventId)) {
                log.info("Event already processed (idempotent): {}", eventId);
                return CompletableFuture.completedFuture(null);
            }

            // Check database idempotency as backup
            if (historyRepository.existsByEventId(eventId)) {
                log.info("Notification already exists in database: {}", eventId);
                idempotencyService.markAsProcessed(eventId);
                return CompletableFuture.completedFuture(null);
            }

            NotificationHistory history = new NotificationHistory();
            history.setEventId(eventId);
            history.setEventType(event.getEventType());
            history.setNotificationType("EMAIL");
            history.setRecipientEmail(event.getEmail());
            history.setRecipientName(buildFullName(event));
            history.setSubject("Welcome to Our Platform!");
            history.setBody(buildWelcomeEmailBody(event));
            history.setStatus("PENDING");
            history.setCorrelationId(MDC.get("correlationId"));

            // Save initial record
            historyRepository.save(history);

            // Send email asynchronously
            return emailService.sendWelcomeEmail(event)
                    .thenRun(() -> {
                        try {
                            // Update history on success
                            history.setStatus("SENT");
                            history.setSentAt(LocalDateTime.now());
                            historyRepository.save(history);
                            
                            // Mark as processed in Redis
                            idempotencyService.markAsProcessed(eventId);
                            
                            notificationSuccessCounter.increment();
                            log.info("Welcome email sent successfully to: {}", event.getEmail());
                        } catch (Exception e) {
                            log.error("Error updating notification history for success: {}", eventId, e);
                        }
                    })
                    .exceptionally(throwable -> {
                        try {
                            // Update history on failure
                            history.setStatus("FAILED");
                            history.setErrorMessage(throwable.getMessage());
                            history.setRetryCount(history.getRetryCount() + 1);
                            historyRepository.save(history);
                            
                            // Mark as failed in Redis
                            idempotencyService.markAsFailed(eventId);
                            
                            notificationFailureCounter.increment();
                            log.error("Failed to send welcome email to: {}", event.getEmail(), throwable);
                        } catch (Exception e) {
                            log.error("Error updating notification history for failure: {}", eventId, e);
                        }
                        return null;
                    });

        } catch (Exception e) {
            notificationFailureCounter.increment();
            log.error("Error processing user created event: {}", eventId, e);
            return CompletableFuture.failedFuture(e);
        } finally {
            sample.stop(notificationProcessingTimer);
        }
    }

    @Transactional
    public void processOrderNotification(OrderNotificationEvent event, String eventId) {
        // Check idempotency
        if (idempotencyService.isProcessed(eventId)) {
            log.info("Event already processed (idempotent): {}", eventId);
            return;
        }

        if (historyRepository.existsByEventId(eventId)) {
            log.info("Notification already exists in database: {}", eventId);
            idempotencyService.markAsProcessed(eventId);
            return;
        }

        NotificationHistory history = new NotificationHistory();
        history.setEventId(eventId);
        history.setEventType("ORDER_" + event.getStatus());
        history.setNotificationType("EMAIL");
        history.setRecipientEmail("user@example.com");  // In real scenario, fetch from user service
        history.setSubject("Order Update: " + event.getStatus());
        history.setBody(event.getMessage());
        history.setStatus("SENT");
        history.setSentAt(LocalDateTime.now());
        history.setCorrelationId(MDC.get("correlationId"));

        historyRepository.save(history);
        idempotencyService.markAsProcessed(eventId);

        log.info("Order notification processed: {} - {}", event.getOrderId(), event.getStatus());
    }

    private String buildFullName(UserCreatedEvent event) {
        StringBuilder name = new StringBuilder();
        if (event.getFirstName() != null && !event.getFirstName().isEmpty()) {
            name.append(event.getFirstName());
        }
        if (event.getLastName() != null && !event.getLastName().isEmpty()) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(event.getLastName());
        }
        if (name.length() == 0) {
            return event.getUsername();
        }
        return name.toString();
    }

    private String buildWelcomeEmailBody(UserCreatedEvent event) {
        String name = buildFullName(event);
        return String.format(
                "Hello %s,\n\n" +
                        "Welcome to our platform! Your account has been successfully created.\n" +
                        "Username: %s\n\n" +
                        "You can now access all features of our service.\n\n" +
                        "Best regards,\n" +
                        "The Team",
                name,
                event.getUsername()
        );
    }
}