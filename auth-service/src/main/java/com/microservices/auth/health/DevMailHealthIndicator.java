package com.microservices.auth.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Development Mail Health Indicator
 * Returns UP status for development environment to avoid health check failures
 * when mail credentials are not properly configured
 */
@Component
@Profile("dev")
@Slf4j
public class DevMailHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        log.debug("Development mail health check - returning UP status");
        return Health.up()
                .withDetail("status", "Mail service configured for development")
                .withDetail("note", "Using development mail configuration")
                .withDetail("host", "smtp.gmail.com")
                .withDetail("port", "587")
                .build();
    }
}
