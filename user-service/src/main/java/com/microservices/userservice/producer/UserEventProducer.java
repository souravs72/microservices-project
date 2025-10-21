package com.microservices.userservice.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.kafka.topic.user-events:user-events}")
    private String userEventsTopic;

    public void publishUserCreated(String username, String email, String firstName, String lastName, String password, String role) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("username", username);
            payload.put("email", email);
            payload.put("firstName", firstName);
            payload.put("lastName", lastName);
            payload.put("password", password);
            payload.put("role", role);
            payload.put("eventType", "USER_CREATED_FROM_USER_SERVICE");
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("source", "user-service");

            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(userEventsTopic, username, message);
            log.info("Published user created event for: {} to topic: {}", username, userEventsTopic);
        } catch (JsonProcessingException e) {
            log.error("Error serializing user created event: {}", e.getMessage(), e);
        }
    }

    public void publishUserUpdated(String username, String email, String firstName, String lastName) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("username", username);
            payload.put("email", email);
            payload.put("firstName", firstName);
            payload.put("lastName", lastName);
            payload.put("eventType", "USER_UPDATED_FROM_USER_SERVICE");
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("source", "user-service");

            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(userEventsTopic, username, message);
            log.info("Published user updated event for: {} to topic: {}", username, userEventsTopic);
        } catch (JsonProcessingException e) {
            log.error("Error serializing user updated event: {}", e.getMessage(), e);
        }
    }
}
