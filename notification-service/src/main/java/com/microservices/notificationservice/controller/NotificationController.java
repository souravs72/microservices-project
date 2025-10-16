package com.microservices.notificationservice.controller;

import com.microservices.notificationservice.dto.UserCreatedEvent;
import com.microservices.notificationservice.entity.NotificationHistory;
import com.microservices.notificationservice.repository.NotificationHistoryRepository;
import com.microservices.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationHistoryRepository historyRepository;
    private final NotificationService notificationService;

    @GetMapping("/history")
    public ResponseEntity<List<NotificationHistory>> getHistory(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status) {

        if (email != null) {
            return ResponseEntity.ok(historyRepository.findByRecipientEmail(email));
        } else if (status != null) {
            return ResponseEntity.ok(historyRepository.findByStatus(status));
        } else {
            return ResponseEntity.ok(historyRepository.findAll());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);

        long sentCount = historyRepository.countSentNotificationsSince(last24Hours);
        long failedCount = historyRepository.countFailedNotificationsSince(last24Hours);
        long totalCount = historyRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalCount);
        stats.put("sent_last_24h", sentCount);
        stats.put("failed_last_24h", failedCount);
        stats.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationHistory> getNotification(@PathVariable Long id) {
        return historyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestBody Map<String, String> request) {
        try {
            // Extract user details from request
            String email = request.getOrDefault("email", "test@example.com");
            String username = request.getOrDefault("username", "testuser");
            String firstName = request.getOrDefault("firstName", "Test");
            String lastName = request.getOrDefault("lastName", "User");

            // Create a UserCreatedEvent to trigger actual email sending
            UserCreatedEvent userEvent = new UserCreatedEvent(
                username,
                email,
                firstName,
                lastName,
                "USER_CREATED",
                System.currentTimeMillis()
            );

            // Generate a unique event ID for this test
            String eventId = "test-email-" + System.currentTimeMillis();
            
            // Process the user event asynchronously to trigger email sending
            notificationService.processUserCreatedEvent(userEvent, eventId)
                .thenRun(() -> log.info("Test email sent successfully to: {}", email))
                .exceptionally(throwable -> {
                    log.error("Failed to send test email to: {}", email, throwable);
                    return null;
                });

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Test email request submitted successfully");
            response.put("email", email);
            response.put("username", username);
            response.put("firstName", firstName);
            response.put("lastName", lastName);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to process test email: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.badRequest().body(response);
        }
    }
}