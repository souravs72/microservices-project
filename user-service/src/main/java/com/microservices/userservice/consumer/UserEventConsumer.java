package com.microservices.userservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.userservice.dto.CreateUserRequest;
import com.microservices.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-events", groupId = "user-service-dev")
    public void handleUserEvent(@Payload String message,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                               @Header(KafkaHeaders.OFFSET) long offset,
                               Acknowledgment acknowledgment) {
        try {
            log.info("Received user event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.debug("Message content: {}", message);

            // Parse the message
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String eventType = (String) eventData.get("eventType");

            switch (eventType) {
                case "USER_CREATED":
                    handleUserCreated(eventData);
                    break;
                case "USER_UPDATED":
                    handleUserUpdated(eventData);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }

            // Acknowledge the message
            acknowledgment.acknowledge();
            log.info("Successfully processed user event from topic: {}, partition: {}, offset: {}", topic, partition, offset);

        } catch (Exception e) {
            log.error("Error processing user event from topic: {}, partition: {}, offset: {}", topic, partition, offset, e);
            // In production, you might want to send to DLQ or retry
            // For now, we'll acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }

    private void handleUserCreated(Map<String, Object> eventData) {
        try {
            String username = (String) eventData.get("username");
            String email = (String) eventData.get("email");
            String firstName = (String) eventData.get("firstName");
            String lastName = (String) eventData.get("lastName");

            log.info("Processing USER_CREATED event for user: {}", username);

            // Create user profile
            CreateUserRequest createUserRequest = new CreateUserRequest();
            createUserRequest.setUsername(username);
            createUserRequest.setEmail(email);
            createUserRequest.setFirstName(firstName);
            createUserRequest.setLastName(lastName);
            createUserRequest.setPassword("MANAGED_BY_AUTH_SERVICE"); // Placeholder

            userService.createUser(createUserRequest);

            log.info("Successfully created user profile for: {}", username);

        } catch (Exception e) {
            log.error("Error creating user profile from event: {}", eventData, e);
            throw e; // Re-throw to trigger error handling
        }
    }

    private void handleUserUpdated(Map<String, Object> eventData) {
        try {
            String username = (String) eventData.get("username");
            String email = (String) eventData.get("email");
            String firstName = (String) eventData.get("firstName");
            String lastName = (String) eventData.get("lastName");

            log.info("Processing USER_UPDATED event for user: {}", username);

            // Update user profile
            // For now, we'll just log it - you can implement update logic later
            log.info("User update event received for: {} - {} {} ({})", username, firstName, lastName, email);

        } catch (Exception e) {
            log.error("Error updating user profile from event: {}", eventData, e);
            throw e; // Re-throw to trigger error handling
        }
    }
}
