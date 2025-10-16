package com.microservices.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.notificationservice.dto.OrderNotificationEvent;
import com.microservices.notificationservice.dto.UserCreatedEvent;
import com.microservices.notificationservice.service.NotificationService;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Counter kafkaMessageConsumedCounter;
    private final Counter kafkaMessageProcessedCounter;
    private final Counter kafkaMessageFailedCounter;

    @Value("${kafka.topics.user-events-dlq}")
    private String userEventsDlq;

    @Value("${kafka.topics.order-notifications-dlq}")
    private String orderNotificationsDlq;

    @Value("${retry.max-attempts:3}")
    private Integer maxRetryAttempts;

    public NotificationConsumer(ObjectMapper objectMapper,
                               NotificationService notificationService,
                               KafkaTemplate<String, String> kafkaTemplate,
                               @Qualifier("kafkaMessageConsumedCounter") Counter kafkaMessageConsumedCounter,
                               @Qualifier("kafkaMessageProcessedCounter") Counter kafkaMessageProcessedCounter,
                               @Qualifier("kafkaMessageFailedCounter") Counter kafkaMessageFailedCounter) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaMessageConsumedCounter = kafkaMessageConsumedCounter;
        this.kafkaMessageProcessedCounter = kafkaMessageProcessedCounter;
        this.kafkaMessageFailedCounter = kafkaMessageFailedCounter;
    }

    @KafkaListener(topics = "${kafka.topics.user-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String correlationId = extractCorrelationId(record);
        MDC.put("correlationId", correlationId);
        kafkaMessageConsumedCounter.increment();

        try {
            log.info("Received user event. Offset: {}, Partition: {}, Key: {}",
                    record.offset(), record.partition(), record.key());

            UserCreatedEvent event = objectMapper.readValue(record.value(), UserCreatedEvent.class);

            // Generate unique event ID for idempotency
            String eventId = generateEventId(record);

            log.info("Processing user created event: {}", event.getUsername());

            if ("USER_CREATED".equals(event.getEventType())) {
                notificationService.processUserCreatedEvent(event, eventId)
                        .thenRun(() -> {
                            kafkaMessageProcessedCounter.increment();
                            acknowledgment.acknowledge();
                            log.info("User event processed successfully and acknowledged");
                        })
                        .exceptionally(throwable -> {
                            kafkaMessageFailedCounter.increment();
                            log.error("Error processing user event. Sending to DLQ", throwable);
                            sendToDLQ(userEventsDlq, record);
                            acknowledgment.acknowledge(); // Acknowledge to prevent reprocessing
                            return null;
                        });
            } else {
                kafkaMessageProcessedCounter.increment();
                acknowledgment.acknowledge();
                log.info("User event processed successfully and acknowledged");
            }

        } catch (Exception e) {
            kafkaMessageFailedCounter.increment();
            log.error("Error processing user event. Sending to DLQ", e);
            sendToDLQ(userEventsDlq, record);
            acknowledgment.acknowledge(); // Acknowledge to prevent reprocessing
        } finally {
            MDC.remove("correlationId");
        }
    }

    @KafkaListener(topics = "${kafka.topics.order-notifications}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrderNotification(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String correlationId = extractCorrelationId(record);
        MDC.put("correlationId", correlationId);

        try {
            log.info("Received order notification. Offset: {}, Partition: {}, Key: {}",
                    record.offset(), record.partition(), record.key());

            OrderNotificationEvent event = objectMapper.readValue(record.value(), OrderNotificationEvent.class);

            String eventId = generateEventId(record);

            log.info("Processing order notification: {} - {}", event.getOrderId(), event.getStatus());

            notificationService.processOrderNotification(event, eventId);

            acknowledgment.acknowledge();
            log.info("Order notification processed successfully and acknowledged");

        } catch (Exception e) {
            log.error("Error processing order notification. Sending to DLQ", e);
            sendToDLQ(orderNotificationsDlq, record);
            acknowledgment.acknowledge();
        } finally {
            MDC.remove("correlationId");
        }
    }

    private String extractCorrelationId(ConsumerRecord<String, String> record) {
        // Try to extract from headers
        if (record.headers() != null) {
            var header = record.headers().lastHeader("correlationId");
            if (header != null && header.value() != null) {
                return new String(header.value());
            }
        }

        // Generate new if not present
        return UUID.randomUUID().toString();
    }

    private String generateEventId(ConsumerRecord<String, String> record) {
        // Generate deterministic event ID based on topic, partition, and offset
        return String.format("%s-%d-%d", record.topic(), record.partition(), record.offset());
    }

    private void sendToDLQ(String dlqTopic, ConsumerRecord<String, String> record) {
        try {
            kafkaTemplate.send(dlqTopic, record.key(), record.value());
            log.info("Message sent to DLQ: {}", dlqTopic);
        } catch (Exception e) {
            log.error("Failed to send message to DLQ: {}", dlqTopic, e);
        }
    }
}