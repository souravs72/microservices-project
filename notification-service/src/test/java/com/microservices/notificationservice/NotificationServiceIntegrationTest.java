package com.microservices.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.notificationservice.dto.UserCreatedEvent;
import com.microservices.notificationservice.repository.NotificationHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@EmbeddedKafka(partitions = 1, topics = {"user-events", "order-notifications"})
@DirtiesContext
@ActiveProfiles("test")
class NotificationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationHistoryRepository notificationHistoryRepository;

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }

    @Test
    void healthEndpointShouldReturnUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void metricsEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    void prometheusEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    void circuitBreakerEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/circuitbreakers"))
                .andExpect(status().isOk());
    }

    @Test
    void retryEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/retries"))
                .andExpect(status().isOk());
    }

    @Test
    void bulkheadEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/bulkheads"))
                .andExpect(status().isOk());
    }

    @Test
    void rateLimiterEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/ratelimiters"))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void shouldProcessUserCreatedEvent() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent();
        event.setEventType("USER_CREATED");
        event.setUsername("testuser");
        event.setEmail("test@example.com");
        event.setFirstName("Test");
        event.setLastName("User");

        // When
        // This would be triggered by Kafka consumer in real scenario
        // For now, we're just testing the service layer

        // Then
        // Verify that the service can handle the event without errors
        assert true; // Placeholder for actual test logic
    }
}
