package com.microservices.auth.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.auth.entity.User;
import com.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @KafkaListener(topics = "user-events", groupId = "auth-service-dev")
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
                case "USER_CREATED_FROM_USER_SERVICE":
                    handleUserCreatedFromUserService(eventData);
                    break;
                case "USER_UPDATED_FROM_USER_SERVICE":
                    handleUserUpdatedFromUserService(eventData);
                    break;
                default:
                    log.debug("Ignoring event type: {} (not relevant to auth service)", eventType);
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

    private void handleUserCreatedFromUserService(Map<String, Object> eventData) {
        try {
            String username = (String) eventData.get("username");
            String email = (String) eventData.get("email");
            String firstName = (String) eventData.get("firstName");
            String lastName = (String) eventData.get("lastName");
            String password = (String) eventData.get("password");
            String roleString = (String) eventData.get("role");

            log.info("Processing USER_CREATED_FROM_USER_SERVICE event for user: {} with role: {}", username, roleString);

            // Check if user already exists in auth database
            if (userRepository.existsByUsername(username)) {
                log.info("User {} already exists in auth database, skipping creation", username);
                return;
            }

            // Parse role from event data, default to USER if not provided or invalid
            User.UserRole role;
            try {
                role = User.UserRole.valueOf(roleString != null ? roleString : "USER");
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role '{}' for user {}, defaulting to USER", roleString, username);
                role = User.UserRole.USER;
            }

            // Create user in auth database
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setEnabled(true);
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setCreatedBy("user-service");
            user.setUpdatedBy("user-service");

            userRepository.save(user);
            
            log.info("Successfully created user in auth database for: {} with role: {}", username, role);

        } catch (Exception e) {
            log.error("Error processing user created event from user service: {}", eventData, e);
            throw e; // Re-throw to trigger error handling
        }
    }

    private void handleUserUpdatedFromUserService(Map<String, Object> eventData) {
        try {
            String username = (String) eventData.get("username");
            String email = (String) eventData.get("email");
            String firstName = (String) eventData.get("firstName");
            String lastName = (String) eventData.get("lastName");

            log.info("Processing USER_UPDATED_FROM_USER_SERVICE event for user: {}", username);

            // Find user in auth database
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user == null) {
                log.warn("User {} not found in auth database, skipping update", username);
                return;
            }

            // Update user fields
            boolean needsUpdate = false;
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
                needsUpdate = true;
            }
            
            if (needsUpdate) {
                user.setUpdatedAt(LocalDateTime.now());
                user.setUpdatedBy("user-service");
                userRepository.save(user);
                log.info("Successfully updated user in auth database for: {}", username);
            } else {
                log.info("No changes needed for user: {}", username);
            }

        } catch (Exception e) {
            log.error("Error processing user updated event from user service: {}", eventData, e);
            throw e; // Re-throw to trigger error handling
        }
    }
}
