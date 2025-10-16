package com.microservices.notificationservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Counter idempotencyCacheHitCounter;
    private final Counter idempotencyCacheMissCounter;

    @Value("${idempotency.ttl-hours:24}")
    private long ttlHours;

    private static final String IDEMPOTENCY_KEY_PREFIX = "notification:processed:";

    public IdempotencyService(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate,
                             @Qualifier("idempotencyCacheHitCounter") Counter idempotencyCacheHitCounter,
                             @Qualifier("idempotencyCacheMissCounter") Counter idempotencyCacheMissCounter) {
        this.redisTemplate = redisTemplate;
        this.idempotencyCacheHitCounter = idempotencyCacheHitCounter;
        this.idempotencyCacheMissCounter = idempotencyCacheMissCounter;
    }

    @CircuitBreaker(name = "redisService")
    @Retry(name = "redisService")
    public boolean isProcessed(String eventId) {
        try {
            String key = IDEMPOTENCY_KEY_PREFIX + eventId;
            Boolean exists = redisTemplate.hasKey(key);
            boolean processed = exists != null && exists;
            
            if (processed) {
                idempotencyCacheHitCounter.increment();
                log.debug("Event already processed (cache hit): {}", eventId);
            } else {
                idempotencyCacheMissCounter.increment();
                log.debug("Event not processed (cache miss): {}", eventId);
            }
            
            return processed;
        } catch (Exception e) {
            log.error("Error checking idempotency for event: {}", eventId, e);
            // In case of Redis failure, assume not processed to avoid duplicate processing
            idempotencyCacheMissCounter.increment();
            return false;
        }
    }

    @CircuitBreaker(name = "redisService")
    @Retry(name = "redisService")
    public void markAsProcessed(String eventId) {
        try {
            String key = IDEMPOTENCY_KEY_PREFIX + eventId;
            redisTemplate.opsForValue().set(key, "processed", ttlHours, TimeUnit.HOURS);
            log.debug("Marked event as processed: {}", eventId);
        } catch (Exception e) {
            log.error("Error marking event as processed: {}", eventId, e);
            // Could implement fallback to database or other storage
        }
    }

    @CircuitBreaker(name = "redisService")
    @Retry(name = "redisService")
    public void markAsFailed(String eventId) {
        try {
            String key = IDEMPOTENCY_KEY_PREFIX + eventId;
            redisTemplate.opsForValue().set(key, "failed", ttlHours, TimeUnit.HOURS);
            log.debug("Marked event as failed: {}", eventId);
        } catch (Exception e) {
            log.error("Error marking event as failed: {}", eventId, e);
        }
    }
}