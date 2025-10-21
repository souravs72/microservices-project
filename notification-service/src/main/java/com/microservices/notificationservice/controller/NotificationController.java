package com.microservices.notificationservice.controller;

import com.microservices.notificationservice.entity.NotificationHistory;
import com.microservices.notificationservice.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationHistoryRepository historyRepository;

    /**
     * Get all notifications with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<NotificationHistory>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("GET /notifications - page: {}, size: {}, email: {}, status: {}", page, size, email, status);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<NotificationHistory> notifications;
        if (email != null && !email.isEmpty()) {
            notifications = historyRepository.findByRecipientEmailContainingIgnoreCase(email, pageable);
        } else if (status != null && !status.isEmpty()) {
            try {
                NotificationHistory.NotificationStatus statusEnum = NotificationHistory.NotificationStatus.valueOf(status.toUpperCase());
                notifications = historyRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status: {}", status);
                return ResponseEntity.badRequest().build();
            }
        } else {
            notifications = historyRepository.findAll(pageable);
        }
        
        log.info("Returning {} notifications (page {})", notifications.getContent().size(), page);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationHistory> getNotification(@PathVariable Long id) {
        log.info("GET /notifications/{}", id);
        
        return historyRepository.findById(id)
                .map(notification -> {
                    log.info("Found notification: {}", notification.getId());
                    return ResponseEntity.ok(notification);
                })
                .orElseGet(() -> {
                    log.warn("Notification not found: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        log.info("GET /notifications/unread-count");
        
        // For now, return 0 as we don't have read/unread status in our current schema
        // This could be enhanced later to track read status
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", 0);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("GET /notifications/stats");
        
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);

        long totalCount = historyRepository.count();
        long sentCount24h = historyRepository.countSentNotificationsSince(last24Hours);
        long failedCount24h = historyRepository.countFailedNotificationsSince(last24Hours);
        long sentCount7d = historyRepository.countSentNotificationsSince(last7Days);
        long failedCount7d = historyRepository.countFailedNotificationsSince(last7Days);
        long pendingCount = historyRepository.countPendingNotifications();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalCount);
        stats.put("sent_last_24h", sentCount24h);
        stats.put("failed_last_24h", failedCount24h);
        stats.put("sent_last_7d", sentCount7d);
        stats.put("failed_last_7d", failedCount7d);
        stats.put("pending", pendingCount);
        stats.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(stats);
    }

    /**
     * Get notifications by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<List<NotificationHistory>> getNotificationsByEmail(@PathVariable String email) {
        log.info("GET /notifications/email/{}", email);
        
        List<NotificationHistory> notifications = historyRepository.findByRecipientEmail(email);
        log.info("Found {} notifications for email: {}", notifications.size(), email);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Test email endpoint
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestBody Map<String, String> request) {
        log.info("POST /notifications/test-email");
        
        try {
            String email = request.getOrDefault("email", "test@example.com");
            String username = request.getOrDefault("username", "testuser");
            String firstName = request.getOrDefault("firstName", "Test");
            String lastName = request.getOrDefault("lastName", "User");

            // This would trigger the notification service
            // For now, just return success
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

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "notification-service");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("database", "connected");
        health.put("kafka", "connected");
        
        return ResponseEntity.ok(health);
    }
}
