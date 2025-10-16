package com.microservices.auth.config;

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
                    log.info("Email circuit breaker state transition: {} -> {}", 
                            event.getStateTransition().getFromState(), 
                            event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> 
                    log.warn("Email circuit breaker failure rate exceeded: {}%", event.getFailureRate()))
                .onCallNotPermitted(event -> 
                    log.warn("Email circuit breaker call not permitted: {}", event.getEventType()));

        return circuitBreaker;
    }

    // Circuit Breaker for Kafka
    @Bean
    public CircuitBreaker kafkaCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(40) // 40% failure rate threshold
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .slidingWindowSize(15)
                .minimumNumberOfCalls(8)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        return CircuitBreaker.of("kafkaService", config);
    }

    // Circuit Breaker for Redis
    @Bean
    public CircuitBreaker redisCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(30) // 30% failure rate threshold
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        return CircuitBreaker.of("redisService", config);
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

    // Retry for Kafka
    @Bean
    public Retry kafkaRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofSeconds(1))
                .retryOnException(throwable -> {
                    log.warn("Retrying Kafka operation due to: {}", throwable.getMessage());
                    return true;
                })
                .build();

        return Retry.of("kafkaService", config);
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

    // Bulkhead for Authentication Operations
    @Bean
    public Bulkhead authBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(20) // Max 20 concurrent auth operations
                .maxWaitDuration(Duration.ofSeconds(5)) // Max wait time
                .build();

        Bulkhead bulkhead = Bulkhead.of("authService", config);
        
        bulkhead.getEventPublisher()
                .onCallPermitted(event -> 
                    log.debug("Auth operation permitted"))
                .onCallRejected(event -> 
                    log.warn("Auth operation rejected due to bulkhead limit"))
                .onCallFinished(event -> 
                    log.debug("Auth operation finished"));

        return bulkhead;
    }

    // Bulkhead for Email Operations
    @Bean
    public Bulkhead emailBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10) // Max 10 concurrent email operations
                .maxWaitDuration(Duration.ofSeconds(5)) // Max wait time
                .build();

        Bulkhead bulkhead = Bulkhead.of("emailService", config);
        
        bulkhead.getEventPublisher()
                .onCallPermitted(event -> 
                    log.debug("Email operation permitted"))
                .onCallRejected(event -> 
                    log.warn("Email operation rejected due to bulkhead limit"))
                .onCallFinished(event -> 
                    log.debug("Email operation finished"));

        return bulkhead;
    }

    // Rate Limiter for Login Attempts
    @Bean
    public RateLimiter loginRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10) // 10 login attempts per period
                .limitRefreshPeriod(Duration.ofMinutes(1)) // 1 minute period
                .timeoutDuration(Duration.ofSeconds(5)) // 5 seconds timeout
                .build();

        RateLimiter rateLimiter = RateLimiter.of("loginService", config);
        
        rateLimiter.getEventPublisher()
                .onSuccess(event -> 
                    log.debug("Login rate limiter call successful"))
                .onFailure(event -> 
                    log.warn("Login rate limiter call failed: {}", event.getEventType()));

        return rateLimiter;
    }

    // Rate Limiter for Registration
    @Bean
    public RateLimiter registrationRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5) // 5 registrations per period
                .limitRefreshPeriod(Duration.ofMinutes(1)) // 1 minute period
                .timeoutDuration(Duration.ofSeconds(5)) // 5 seconds timeout
                .build();

        RateLimiter rateLimiter = RateLimiter.of("registrationService", config);
        
        rateLimiter.getEventPublisher()
                .onSuccess(event -> 
                    log.debug("Registration rate limiter call successful"))
                .onFailure(event -> 
                    log.warn("Registration rate limiter call failed: {}", event.getEventType()));

        return rateLimiter;
    }

    // Rate Limiter for Password Reset
    @Bean
    public RateLimiter passwordResetRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(3) // 3 password reset attempts per period
                .limitRefreshPeriod(Duration.ofMinutes(5)) // 5 minute period
                .timeoutDuration(Duration.ofSeconds(5)) // 5 seconds timeout
                .build();

        RateLimiter rateLimiter = RateLimiter.of("passwordResetService", config);
        
        rateLimiter.getEventPublisher()
                .onSuccess(event -> 
                    log.debug("Password reset rate limiter call successful"))
                .onFailure(event -> 
                    log.warn("Password reset rate limiter call failed: {}", event.getEventType()));

        return rateLimiter;
    }
}
