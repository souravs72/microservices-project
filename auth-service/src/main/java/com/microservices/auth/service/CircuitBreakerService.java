package com.microservices.auth.service;

import com.microservices.auth.client.UserServiceClient;
import com.microservices.auth.dto.CreateUserProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class CircuitBreakerService {

    private final UserServiceClient userServiceClient;

    @Value("${app.circuit-breaker.failure-threshold:5}")
    private int failureThreshold;

    @Value("${app.circuit-breaker.timeout-seconds:60}")
    private long timeoutSeconds;

    @Value("${app.circuit-breaker.half-open-max-calls:3}")
    private int halfOpenMaxCalls;

    // Circuit breaker state for User Service
    private final ConcurrentHashMap<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();

    public enum CircuitState {
        CLOSED,    // Normal operation
        OPEN,      // Circuit is open, failing fast
        HALF_OPEN  // Testing if service is back
    }

    private static class CircuitBreakerState {
        private volatile CircuitState state = CircuitState.CLOSED;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicLong lastFailureTime = new AtomicLong(0);
        private final AtomicInteger halfOpenCalls = new AtomicInteger(0);

        public CircuitState getState() {
            return state;
        }

        public void setState(CircuitState state) {
            this.state = state;
        }

        public AtomicInteger getFailureCount() {
            return failureCount;
        }

        public AtomicInteger getSuccessCount() {
            return successCount;
        }

        public AtomicLong getLastFailureTime() {
            return lastFailureTime;
        }

        public AtomicInteger getHalfOpenCalls() {
            return halfOpenCalls;
        }
    }

    /**
     * Execute user service call with circuit breaker protection
     */
    public boolean executeUserServiceCall(CreateUserProfileRequest request, String apiKey) {
        String serviceName = "user-service";
        CircuitBreakerState state = circuitBreakers.computeIfAbsent(serviceName, k -> new CircuitBreakerState());

        // Check circuit state
        if (state.getState() == CircuitState.OPEN) {
            if (isTimeoutExpired(state)) {
                state.setState(CircuitState.HALF_OPEN);
                state.getHalfOpenCalls().set(0);
                log.info("Circuit breaker for {} moved to HALF_OPEN state", serviceName);
            } else {
                log.warn("Circuit breaker for {} is OPEN, failing fast", serviceName);
                return false;
            }
        }

        // Check half-open state
        if (state.getState() == CircuitState.HALF_OPEN) {
            if (state.getHalfOpenCalls().get() >= halfOpenMaxCalls) {
                log.warn("Half-open circuit breaker for {} exceeded max calls, failing fast", serviceName);
                return false;
            }
            state.getHalfOpenCalls().incrementAndGet();
        }

        try {
            // Make the actual call
            userServiceClient.createUserProfile(request, apiKey);
            
            // Success - reset failure count and update state
            state.getFailureCount().set(0);
            state.getSuccessCount().incrementAndGet();
            
            if (state.getState() == CircuitState.HALF_OPEN) {
                state.setState(CircuitState.CLOSED);
                log.info("Circuit breaker for {} moved to CLOSED state after successful call", serviceName);
            }
            
            return true;

        } catch (Exception e) {
            // Failure - increment failure count
            int failures = state.getFailureCount().incrementAndGet();
            state.getLastFailureTime().set(System.currentTimeMillis());
            
            log.warn("User service call failed (failure count: {}): {}", failures, e.getMessage());
            
            // Check if we should open the circuit
            if (failures >= failureThreshold) {
                state.setState(CircuitState.OPEN);
                log.error("Circuit breaker for {} moved to OPEN state after {} failures", serviceName, failures);
            }
            
            return false;
        }
    }

    /**
     * Check if timeout has expired for open circuit
     */
    private boolean isTimeoutExpired(CircuitBreakerState state) {
        long lastFailureTime = state.getLastFailureTime().get();
        if (lastFailureTime == 0) {
            return true;
        }
        
        long timeoutMillis = timeoutSeconds * 1000;
        return System.currentTimeMillis() - lastFailureTime > timeoutMillis;
    }

    /**
     * Get circuit breaker status for monitoring
     */
    public CircuitBreakerStatus getCircuitBreakerStatus(String serviceName) {
        CircuitBreakerState state = circuitBreakers.get(serviceName);
        if (state == null) {
            return new CircuitBreakerStatus(serviceName, CircuitState.CLOSED, 0, 0, 0, null);
        }

        return new CircuitBreakerStatus(
                serviceName,
                state.getState(),
                state.getFailureCount().get(),
                state.getSuccessCount().get(),
                state.getHalfOpenCalls().get(),
                state.getLastFailureTime().get() > 0 ? 
                        LocalDateTime.ofEpochSecond(state.getLastFailureTime().get() / 1000, 0, java.time.ZoneOffset.UTC) : 
                        null
        );
    }

    /**
     * Reset circuit breaker (for testing or manual intervention)
     */
    public void resetCircuitBreaker(String serviceName) {
        CircuitBreakerState state = circuitBreakers.get(serviceName);
        if (state != null) {
            state.setState(CircuitState.CLOSED);
            state.getFailureCount().set(0);
            state.getSuccessCount().set(0);
            state.getHalfOpenCalls().set(0);
            state.getLastFailureTime().set(0);
            log.info("Circuit breaker for {} has been reset", serviceName);
        }
    }

    /**
     * Circuit breaker status DTO
     */
    public static class CircuitBreakerStatus {
        private final String serviceName;
        private final CircuitState state;
        private final int failureCount;
        private final int successCount;
        private final int halfOpenCalls;
        private final LocalDateTime lastFailureTime;

        public CircuitBreakerStatus(String serviceName, CircuitState state, int failureCount, 
                                   int successCount, int halfOpenCalls, LocalDateTime lastFailureTime) {
            this.serviceName = serviceName;
            this.state = state;
            this.failureCount = failureCount;
            this.successCount = successCount;
            this.halfOpenCalls = halfOpenCalls;
            this.lastFailureTime = lastFailureTime;
        }

        // Getters
        public String getServiceName() { return serviceName; }
        public CircuitState getState() { return state; }
        public int getFailureCount() { return failureCount; }
        public int getSuccessCount() { return successCount; }
        public int getHalfOpenCalls() { return halfOpenCalls; }
        public LocalDateTime getLastFailureTime() { return lastFailureTime; }
    }
}






