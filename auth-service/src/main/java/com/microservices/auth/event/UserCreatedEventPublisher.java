package com.microservices.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedEventPublisher {

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${app.security.internal-api-key:change-me}")
    private String internalApiKey;

    public void publishUserCreated(String username, String email, String firstName, String lastName) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("email", email);
            userData.put("password", "MANAGED_BY_AUTH_SERVICE"); // Placeholder
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);

            // Fixed: Changed from /internal/sync to /sync
            String url = userServiceUrl + "/api/users/sync";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-API-Key", internalApiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(userData, headers);
            restTemplate.postForEntity(url, requestEntity, Void.class);

            log.info("User profile synced to User Service: {}", username);
        } catch (Exception e) {
            log.error("Failed to sync user to User Service: {}", e.getMessage());
            // In production, use message queue retry mechanism
        }
    }
}