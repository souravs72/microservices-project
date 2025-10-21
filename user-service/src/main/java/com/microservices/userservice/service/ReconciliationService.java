package com.microservices.userservice.service;

import com.microservices.userservice.dto.UserDTO;
import com.microservices.userservice.entity.User;
import com.microservices.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${app.reconciliation.enabled:true}")
    private boolean reconciliationEnabled;

    @Value("${app.reconciliation.batch-size:100}")
    private int batchSize;

    @Value("${app.reconciliation.max-age-hours:24}")
    private int maxAgeHours;

    // Metrics for monitoring
    private final AtomicLong reconciliationCount = new AtomicLong(0);
    private final AtomicLong lastReconciliationTime = new AtomicLong(0);
    private final Map<String, Long> reconciliationMetrics = new ConcurrentHashMap<>();

    /**
     * Run reconciliation every hour to ensure data consistency
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void performReconciliation() {
        if (!reconciliationEnabled) {
            log.debug("Reconciliation is disabled");
            return;
        }

        try {
            log.info("Starting user data reconciliation");
            long startTime = System.currentTimeMillis();

            // Get users that might need reconciliation
            List<User> usersToCheck = userRepository.findUsersForReconciliation(
                    LocalDateTime.now().minusHours(maxAgeHours), 
                    org.springframework.data.domain.PageRequest.of(0, batchSize)
            );

            int reconciledCount = 0;
            int errorCount = 0;

            for (User user : usersToCheck) {
                try {
                    if (needsReconciliation(user)) {
                        reconcileUser(user);
                        reconciledCount++;
                    }
                } catch (Exception e) {
                    log.error("Error reconciling user {}: {}", user.getUsername(), e.getMessage());
                    errorCount++;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            reconciliationCount.incrementAndGet();
            lastReconciliationTime.set(System.currentTimeMillis());

            // Update metrics
            reconciliationMetrics.put("lastReconciliationDuration", duration);
            reconciliationMetrics.put("usersChecked", (long) usersToCheck.size());
            reconciliationMetrics.put("usersReconciled", (long) reconciledCount);
            reconciliationMetrics.put("errors", (long) errorCount);

            log.info("Reconciliation completed: {} users checked, {} reconciled, {} errors, duration: {}ms",
                    usersToCheck.size(), reconciledCount, errorCount, duration);

        } catch (Exception e) {
            log.error("Error during reconciliation: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if a user needs reconciliation
     */
    private boolean needsReconciliation(User user) {
        // Check various conditions that might indicate inconsistency
        if (user.getCreatedAt() == null || user.getUpdatedAt() == null) {
            return true;
        }

        // Check if user has missing required fields
        if (user.getUsername() == null || user.getEmail() == null) {
            return true;
        }

        // Check if user was created recently and might not be fully synchronized
        if (user.getCreatedAt().isAfter(LocalDateTime.now().minusHours(1))) {
            return true;
        }

        // Check if user was last updated a long time ago but should be active
        if (user.getActive() && user.getUpdatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
            return true;
        }

        return false;
    }

    /**
     * Reconcile a single user
     */
    private void reconcileUser(User user) {
        log.debug("Reconciling user: {}", user.getUsername());

        boolean needsUpdate = false;

        // Ensure required fields are set
        if (user.getMemberSince() == null) {
            user.setMemberSince(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now());
            needsUpdate = true;
        }

        if (user.getRole() == null) {
            user.setRole(User.UserRole.USER);
            needsUpdate = true;
        }

        // Ensure audit fields are properly set
        if (user.getCreatedBy() == null) {
            user.setCreatedBy("system");
            needsUpdate = true;
        }

        if (user.getUpdatedBy() == null) {
            user.setUpdatedBy("system");
            needsUpdate = true;
        }

        // Update timestamps if needed
        if (user.getUpdatedAt() == null) {
            user.setUpdatedAt(LocalDateTime.now());
            needsUpdate = true;
        }

        if (needsUpdate) {
            user.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedBy("reconciliation-service");
            userRepository.save(user);
            log.info("Reconciled user: {}", user.getUsername());
        }
    }

    /**
     * Manual reconciliation for a specific user
     */
    @Transactional
    public UserDTO reconcileUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        reconcileUser(user);
        return userService.convertToDTO(user);
    }

    /**
     * Get reconciliation metrics
     */
    public Map<String, Object> getReconciliationMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        
        metrics.put("enabled", reconciliationEnabled);
        metrics.put("totalReconciliations", reconciliationCount.get());
        metrics.put("lastReconciliationTime", lastReconciliationTime.get());
        metrics.put("lastReconciliationDuration", reconciliationMetrics.getOrDefault("lastReconciliationDuration", 0L));
        metrics.put("lastUsersChecked", reconciliationMetrics.getOrDefault("usersChecked", 0L));
        metrics.put("lastUsersReconciled", reconciliationMetrics.getOrDefault("usersReconciled", 0L));
        metrics.put("lastErrors", reconciliationMetrics.getOrDefault("errors", 0L));
        
        // Calculate health score
        long totalChecked = reconciliationMetrics.getOrDefault("usersChecked", 0L);
        long totalErrors = reconciliationMetrics.getOrDefault("errors", 0L);
        
        if (totalChecked > 0) {
            double errorRate = (double) totalErrors / totalChecked;
            String healthStatus = errorRate < 0.01 ? "HEALTHY" : 
                                 errorRate < 0.05 ? "DEGRADED" : "UNHEALTHY";
            metrics.put("healthStatus", healthStatus);
            metrics.put("errorRate", errorRate);
        } else {
            metrics.put("healthStatus", "UNKNOWN");
            metrics.put("errorRate", 0.0);
        }
        
        return metrics;
    }

    /**
     * Force reconciliation for all users (use with caution)
     */
    @Transactional
    public Map<String, Object> forceFullReconciliation() {
        log.warn("Starting forced full reconciliation - this may take a while");
        
        long startTime = System.currentTimeMillis();
        long totalUsers = userRepository.count();
        int reconciledCount = 0;
        int errorCount = 0;

        // Process in batches to avoid memory issues
        int batchNumber = 0;
        int batchSize = 50;

        while (true) {
            List<User> batch = userRepository.findAll()
                    .stream()
                    .skip((long) batchNumber * batchSize)
                    .limit(batchSize)
                    .toList();

            if (batch.isEmpty()) {
                break;
            }

            for (User user : batch) {
                try {
                    reconcileUser(user);
                    reconciledCount++;
                } catch (Exception e) {
                    log.error("Error reconciling user {}: {}", user.getUsername(), e.getMessage());
                    errorCount++;
                }
            }

            batchNumber++;
            log.info("Processed batch {} - {} users reconciled so far", batchNumber, reconciledCount);
        }

        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("totalUsers", totalUsers);
        result.put("reconciledUsers", reconciledCount);
        result.put("errors", errorCount);
        result.put("duration", duration);
        result.put("timestamp", LocalDateTime.now());

        log.info("Forced reconciliation completed: {} users processed, {} reconciled, {} errors, duration: {}ms",
                totalUsers, reconciledCount, errorCount, duration);

        return result;
    }
}
