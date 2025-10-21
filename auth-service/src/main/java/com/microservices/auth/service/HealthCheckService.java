package com.microservices.auth.service;

import com.microservices.auth.client.UserServiceClient;
import com.microservices.auth.repository.OutboxEventRepository;
import com.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserServiceClient userServiceClient;
    private final CircuitBreakerService circuitBreakerService;
    private final OutboxEventProcessor outboxEventProcessor;

    @Value("${app.health.user-service-timeout:5000}")
    private long userServiceTimeoutMs;

    /**
     * Comprehensive health check
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Database health
            health.put("database", checkDatabaseHealth());
            
            // Outbox health
            health.put("outbox", outboxEventProcessor.getOutboxHealth());
            
            // User Service health
            health.put("userService", checkUserServiceHealth());
            
            // Circuit breaker status
            health.put("circuitBreaker", circuitBreakerService.getCircuitBreakerStatus("user-service"));
            
            // Overall health status
            health.put("overallStatus", determineOverallHealth(health));
            health.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error during health check: {}", e.getMessage(), e);
            health.put("error", e.getMessage());
            health.put("overallStatus", "ERROR");
            health.put("timestamp", LocalDateTime.now());
        }
        
        return health;
    }

    /**
     * Check database connectivity and performance
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Test basic connectivity
            long userCount = userRepository.count();
            long outboxCount = outboxEventRepository.count();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            dbHealth.put("status", "HEALTHY");
            dbHealth.put("responseTimeMs", responseTime);
            dbHealth.put("userCount", userCount);
            dbHealth.put("outboxEventCount", outboxCount);
            
            if (responseTime > 1000) {
                dbHealth.put("status", "DEGRADED");
                dbHealth.put("warning", "Slow database response");
            }
            
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            dbHealth.put("status", "UNHEALTHY");
            dbHealth.put("error", e.getMessage());
        }
        
        return dbHealth;
    }

    /**
     * Check User Service connectivity
     */
    private Map<String, Object> checkUserServiceHealth() {
        Map<String, Object> userServiceHealth = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Test User Service connectivity with timeout
            boolean isReachable = testUserServiceConnectivity();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            userServiceHealth.put("status", isReachable ? "HEALTHY" : "UNHEALTHY");
            userServiceHealth.put("responseTimeMs", responseTime);
            userServiceHealth.put("reachable", isReachable);
            
            if (responseTime > userServiceTimeoutMs) {
                userServiceHealth.put("status", "DEGRADED");
                userServiceHealth.put("warning", "Slow User Service response");
            }
            
        } catch (Exception e) {
            log.error("User Service health check failed: {}", e.getMessage());
            userServiceHealth.put("status", "UNHEALTHY");
            userServiceHealth.put("error", e.getMessage());
        }
        
        return userServiceHealth;
    }

    /**
     * Test User Service connectivity
     */
    private boolean testUserServiceConnectivity() {
        try {
            // Simple ping to User Service
            // This would be a lightweight endpoint like /health/ping
            // For now, we'll use a timeout-based approach
            
            Thread timeoutThread = new Thread(() -> {
                try {
                    Thread.sleep(userServiceTimeoutMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            timeoutThread.start();
            
            // In a real implementation, you would make an actual HTTP call here
            // For now, we'll simulate based on circuit breaker state
            CircuitBreakerService.CircuitBreakerStatus status = 
                    circuitBreakerService.getCircuitBreakerStatus("user-service");
            
            timeoutThread.interrupt();
            
            return status.getState() != CircuitBreakerService.CircuitState.OPEN;
            
        } catch (Exception e) {
            log.warn("User Service connectivity test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Determine overall health status
     */
    private String determineOverallHealth(Map<String, Object> health) {
        try {
            // Check database status
            Map<String, Object> dbHealth = (Map<String, Object>) health.get("database");
            String dbStatus = (String) dbHealth.get("status");
            
            // Check outbox status
            Map<String, Object> outboxHealth = (Map<String, Object>) health.get("outbox");
            String outboxStatus = (String) outboxHealth.get("healthStatus");
            
            // Check User Service status
            Map<String, Object> userServiceHealth = (Map<String, Object>) health.get("userService");
            String userServiceStatus = (String) userServiceHealth.get("status");
            
            // Check circuit breaker status
            CircuitBreakerService.CircuitBreakerStatus circuitStatus = 
                    (CircuitBreakerService.CircuitBreakerStatus) health.get("circuitBreaker");
            
            // Determine overall status
            if ("UNHEALTHY".equals(dbStatus) || "UNHEALTHY".equals(userServiceStatus)) {
                return "UNHEALTHY";
            }
            
            if ("DEGRADED".equals(dbStatus) || "DEGRADED".equals(userServiceStatus) || 
                "DEGRADED".equals(outboxStatus) || 
                circuitStatus.getState() == CircuitBreakerService.CircuitState.OPEN) {
                return "DEGRADED";
            }
            
            return "HEALTHY";
            
        } catch (Exception e) {
            log.error("Error determining overall health: {}", e.getMessage());
            return "ERROR";
        }
    }

    /**
     * Get detailed synchronization status
     */
    public Map<String, Object> getSynchronizationStatus() {
        Map<String, Object> syncStatus = new HashMap<>();
        
        try {
            // Outbox metrics
            Map<String, Object> outboxHealth = outboxEventProcessor.getOutboxHealth();
            syncStatus.put("outbox", outboxHealth);
            
            // Circuit breaker status
            CircuitBreakerService.CircuitBreakerStatus circuitStatus = 
                    circuitBreakerService.getCircuitBreakerStatus("user-service");
            syncStatus.put("circuitBreaker", circuitStatus);
            
            // User Service connectivity
            Map<String, Object> userServiceHealth = checkUserServiceHealth();
            syncStatus.put("userService", userServiceHealth);
            
            // Sync recommendations
            syncStatus.put("recommendations", generateSyncRecommendations(outboxHealth, circuitStatus, userServiceHealth));
            
            syncStatus.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error getting synchronization status: {}", e.getMessage(), e);
            syncStatus.put("error", e.getMessage());
            syncStatus.put("timestamp", LocalDateTime.now());
        }
        
        return syncStatus;
    }

    /**
     * Generate synchronization recommendations
     */
    private Map<String, Object> generateSyncRecommendations(Map<String, Object> outboxHealth, 
                                                           CircuitBreakerService.CircuitBreakerStatus circuitStatus,
                                                           Map<String, Object> userServiceHealth) {
        Map<String, Object> recommendations = new HashMap<>();
        
        try {
            long unprocessedEvents = (Long) outboxHealth.get("unprocessedEvents");
            long failedEvents = (Long) outboxHealth.get("failedEvents");
            
            if (unprocessedEvents > 10) {
                recommendations.put("outbox", "High number of unprocessed events. Consider increasing processing frequency.");
            }
            
            if (failedEvents > 0) {
                recommendations.put("failedEvents", "Some events have failed. Check User Service connectivity and retry failed events.");
            }
            
            if (circuitStatus.getState() == CircuitBreakerService.CircuitState.OPEN) {
                recommendations.put("circuitBreaker", "Circuit breaker is open. User Service may be down. Check service health.");
            }
            
            if (!"HEALTHY".equals(userServiceHealth.get("status"))) {
                recommendations.put("userService", "User Service is not healthy. Check service logs and connectivity.");
            }
            
            if (recommendations.isEmpty()) {
                recommendations.put("status", "All systems operating normally");
            }
            
        } catch (Exception e) {
            log.error("Error generating recommendations: {}", e.getMessage());
            recommendations.put("error", "Unable to generate recommendations");
        }
        
        return recommendations;
    }
}






