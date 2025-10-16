package com.microservices.notificationservice.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Health health() {
        try {
            // Test Redis connection with timeout
            String testKey = "health:check:" + System.currentTimeMillis();
            String testValue = "test";
            
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(10));
            String retrievedValue = redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            
            if (testValue.equals(retrievedValue)) {
                return Health.up()
                        .withDetail("status", "Redis is available")
                        .withDetail("test", "Connection and read/write operations successful")
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Redis read/write test failed")
                        .withDetail("expected", testValue)
                        .withDetail("actual", retrievedValue)
                        .build();
            }
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                    .withDetail("status", "Redis is not available")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
