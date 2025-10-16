package com.microservices.notificationservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class ResilienceConfig {

    // Circuit Breaker for Email Service
    @Bean
    public CircuitBreaker emailCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate threshold
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30 seconds before trying again
                .slidingWindowSize(10) // Last 10 calls
                .minimumNumberOfCalls(5) // Minimum 5 calls before calculating failure rate
                .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 calls in half-open state
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("emailService", config);
        
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.info("Circuit breaker state transition: {} -> {}", 
                            event.getStateTransition().getFromState(), 
                            event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> 
                    log.warn("Circuit breaker failure rate exceeded: {}%", event.getFailureRate()))
                .onCallNotPermitted(event -> 
                    log.warn("Circuit breaker call not permitted: {}", event.getEventType()));

        return circuitBreaker;
    }

    // Retry for Email Service
    @Bean
    public Retry emailRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryOnException(throwable -> {
                    log.warn("Retrying email service call due to: {}", throwable.getMessage());
                    return true;
                })
                .build();

        Retry retry = Retry.of("emailService", config);
        
        retry.getEventPublisher()
                .onRetry(event -> 
                    log.info("Retrying email service call, attempt: {}", event.getNumberOfRetryAttempts()))
                .onError(event -> 
                    log.error("Retry failed for email service: {}", event.getLastThrowable().getMessage()));

        return retry;
    }

    // Bulkhead for Email Service (limit concurrent calls)
    @Bean
    public Bulkhead emailBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10) // Max 10 concurrent email calls
                .maxWaitDuration(Duration.ofSeconds(5)) // Max wait time
                .build();

        Bulkhead bulkhead = Bulkhead.of("emailService", config);
        
        bulkhead.getEventPublisher()
                .onCallPermitted(event -> 
                    log.debug("Email service call permitted"))
                .onCallRejected(event -> 
                    log.warn("Email service call rejected due to bulkhead limit"))
                .onCallFinished(event -> 
                    log.debug("Email service call finished"));

        return bulkhead;
    }

    // Rate Limiter for Email Service
    @Bean
    public RateLimiter emailRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100) // 100 calls per period
                .limitRefreshPeriod(Duration.ofMinutes(1)) // 1 minute period
                .timeoutDuration(Duration.ofSeconds(5)) // 5 seconds timeout
                .build();

        RateLimiter rateLimiter = RateLimiter.of("emailService", config);
        
        rateLimiter.getEventPublisher()
                .onSuccess(event -> 
                    log.debug("Email service rate limiter call successful"))
                .onFailure(event -> 
                    log.warn("Email service rate limiter call failed: {}", event.getEventType()));

        return rateLimiter;
    }

    // Circuit Breaker for Redis
    @Bean
    public CircuitBreaker redisCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(30) // 30% failure rate threshold
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        return CircuitBreaker.of("redisService", config);
    }

    // Retry for Redis
    @Bean
    public Retry redisRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(500))
                .retryOnException(throwable -> {
                    log.warn("Retrying Redis operation due to: {}", throwable.getMessage());
                    return true;
                })
                .build();

        return Retry.of("redisService", config);
    }
}
