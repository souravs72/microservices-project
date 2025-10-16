package com.microservices.userservice.config;

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

    // Circuit Breaker for Auth Service Communication
    @Bean
    public CircuitBreaker authServiceCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate threshold
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30 seconds before trying again
                .slidingWindowSize(10) // Last 10 calls
                .minimumNumberOfCalls(5) // Minimum 5 calls before calculating failure rate
                .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 calls in half-open state
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("authService", config);
        
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.info("Auth service circuit breaker state transition: {} -> {}", 
                            event.getStateTransition().getFromState(), 
                            event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> 
                    log.warn("Auth service circuit breaker failure rate exceeded: {}%", event.getFailureRate()))
                .onCallNotPermitted(event -> 
                    log.warn("Auth service circuit breaker call not permitted: {}", event.getEventType()));

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

    // Retry for Auth Service Communication
    @Bean
    public Retry authServiceRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryOnException(throwable -> {
                    log.warn("Retrying auth service call due to: {}", throwable.getMessage());
                    return true;
                })
                .build();

        Retry retry = Retry.of("authService", config);
        
        retry.getEventPublisher()
                .onRetry(event -> 
                    log.info("Retrying auth service call, attempt: {}", event.getNumberOfRetryAttempts()))
                .onError(event -> 
                    log.error("Retry failed for auth service: {}", event.getLastThrowable().getMessage()));

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

    // Bulkhead for User Operations
    @Bean
    public Bulkhead userOperationsBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(30) // Max 30 concurrent user operations
                .maxWaitDuration(Duration.ofSeconds(5)) // Max wait time
                .build();

        Bulkhead bulkhead = Bulkhead.of("userOperations", config);
        
        bulkhead.getEventPublisher()
                .onCallPermitted(event -> 
                    log.debug("User operation permitted"))
                .onCallRejected(event -> 
                    log.warn("User operation rejected due to bulkhead limit"))
                .onCallFinished(event -> 
                    log.debug("User operation finished"));

        return bulkhead;
    }

    // Bulkhead for GraphQL Operations
    @Bean
    public Bulkhead graphqlBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(20) // Max 20 concurrent GraphQL operations
                .maxWaitDuration(Duration.ofSeconds(5)) // Max wait time
                .build();

        Bulkhead bulkhead = Bulkhead.of("graphqlOperations", config);
        
        bulkhead.getEventPublisher()
                .onCallPermitted(event -> 
                    log.debug("GraphQL operation permitted"))
                .onCallRejected(event -> 
                    log.warn("GraphQL operation rejected due to bulkhead limit"))
                .onCallFinished(event -> 
                    log.debug("GraphQL operation finished"));

        return bulkhead;
    }

    // Rate Limiter for User Creation
    @Bean
    public RateLimiter userCreationRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(20) // 20 user creations per period
                .limitRefreshPeriod(Duration.ofMinutes(1)) // 1 minute period
                .timeoutDuration(Duration.ofSeconds(5)) // 5 seconds timeout
                .build();

        RateLimiter rateLimiter = RateLimiter.of("userCreation", config);
        
        rateLimiter.getEventPublisher()
                .onSuccess(event -> 
                    log.debug("User creation rate limiter call successful"))
                .onFailure(event -> 
                    log.warn("User creation rate limiter call failed: {}", event.getEventType()));

        return rateLimiter;
    }

    // Rate Limiter for User Updates
    @Bean
    public RateLimiter userUpdateRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(50) // 50 user updates per period
                .limitRefreshPeriod(Duration.ofMinutes(1)) // 1 minute period
                .timeoutDuration(Duration.ofSeconds(5)) // 5 seconds timeout
                .build();

        RateLimiter rateLimiter = RateLimiter.of("userUpdate", config);
        
        rateLimiter.getEventPublisher()
                .onSuccess(event -> 
                    log.debug("User update rate limiter call successful"))
                .onFailure(event -> 
                    log.warn("User update rate limiter call failed: {}", event.getEventType()));

        return rateLimiter;
    }

    // Rate Limiter for GraphQL Queries
    @Bean
    public RateLimiter graphqlQueryRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100) // 100 GraphQL queries per period
                .limitRefreshPeriod(Duration.ofMinutes(1)) // 1 minute period
                .timeoutDuration(Duration.ofSeconds(5)) // 5 seconds timeout
                .build();

        RateLimiter rateLimiter = RateLimiter.of("graphqlQuery", config);
        
        rateLimiter.getEventPublisher()
                .onSuccess(event -> 
                    log.debug("GraphQL query rate limiter call successful"))
                .onFailure(event -> 
                    log.warn("GraphQL query rate limiter call failed: {}", event.getEventType()));

        return rateLimiter;
    }
}
