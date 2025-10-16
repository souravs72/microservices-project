
package com.microservices.auth.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.auth.dto.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String USER_EVENTS_TOPIC = "user-events";

    public void publishUserCreated(UserCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_EVENTS_TOPIC, event.getUsername(), message);
            log.info("Published user created event for: {}", event.getUsername());
        } catch (JsonProcessingException e) {
            log.error("Error serializing user created event: {}", e.getMessage(), e);
        }
    }
}
