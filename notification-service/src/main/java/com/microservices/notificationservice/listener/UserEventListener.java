package com.microservices.notificationservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.notificationservice.dto.UserCreatedEvent;
import com.microservices.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.user-events}", groupId = "notification-service")
    public void handleUserEvent(@Payload String message,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                               @Header(KafkaHeaders.OFFSET) long offset,
                               Acknowledgment acknowledgment) {
        
        try {
            log.info("Received message from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.debug("Message content: {}", message);

            // Parse the message
            UserCreatedEvent userEvent = objectMapper.readValue(message, UserCreatedEvent.class);
            
            log.info("Processing user event: {} for user: {}", userEvent.getEventType(), userEvent.getUsername());

            // Process the notification
            String eventId = generateEventId(userEvent);
            notificationService.processUserCreatedEvent(userEvent, eventId)
                .thenRun(() -> {
                    log.info("Successfully processed user event: {} for user: {}", 
                        userEvent.getEventType(), userEvent.getUsername());
                    acknowledgment.acknowledge();
                })
                .exceptionally(throwable -> {
                    log.error("Failed to process user event: {} for user: {}", 
                        userEvent.getEventType(), userEvent.getUsername(), throwable);
                    // Don't acknowledge - let it retry
                    return null;
                });

        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", message, e);
            // Don't acknowledge - let it retry
        }
    }

    private String generateEventId(UserCreatedEvent userEvent) {
        return String.format("%s-%s-%d", 
            userEvent.getEventType(), 
            userEvent.getUsername(), 
            System.currentTimeMillis());
    }
}
