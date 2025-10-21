package com.microservices.notificationservice.controller;

import com.microservices.notificationservice.dto.UserCreatedEvent;
import com.microservices.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/notifications")
@RequiredArgsConstructor
@Slf4j
public class InternalNotificationController {

    private final NotificationService notificationService;

    /**
     * Internal endpoint for sending notifications (service-to-service)
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody UserCreatedEvent userEvent) {
        log.info("POST /api/internal/notifications/send - User: {}", userEvent.getUsername());
        
        try {
            String eventId = generateEventId(userEvent);
            
            notificationService.processUserCreatedEvent(userEvent, eventId)
                .thenRun(() -> log.info("Internal notification sent successfully for user: {}", userEvent.getUsername()))
                .exceptionally(throwable -> {
                    log.error("Failed to send internal notification for user: {}", userEvent.getUsername(), throwable);
                    return null;
                });

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification request submitted successfully");
            response.put("eventId", eventId);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing internal notification request", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to process notification request: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Health check for internal API
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "notification-service-internal");
        health.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(health);
    }

    private String generateEventId(UserCreatedEvent userEvent) {
        return String.format("internal-%s-%s-%d", 
            userEvent.getEventType(), 
            userEvent.getUsername(), 
            System.currentTimeMillis());
    }
}
