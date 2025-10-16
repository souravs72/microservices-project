package com.microservices.auth.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    @Bean
    public Counter loginAttemptsCounter() {
        return Counter.builder("auth.login.attempts")
                .description("Total number of login attempts")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter loginSuccessCounter() {
        return Counter.builder("auth.login.success")
                .description("Total number of successful logins")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter loginFailureCounter() {
        return Counter.builder("auth.login.failures")
                .description("Total number of failed logins")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter registrationAttemptsCounter() {
        return Counter.builder("auth.registration.attempts")
                .description("Total number of registration attempts")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter registrationSuccessCounter() {
        return Counter.builder("auth.registration.success")
                .description("Total number of successful registrations")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter registrationFailureCounter() {
        return Counter.builder("auth.registration.failures")
                .description("Total number of failed registrations")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter passwordResetAttemptsCounter() {
        return Counter.builder("auth.password.reset.attempts")
                .description("Total number of password reset attempts")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter passwordResetSuccessCounter() {
        return Counter.builder("auth.password.reset.success")
                .description("Total number of successful password resets")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter passwordResetFailureCounter() {
        return Counter.builder("auth.password.reset.failures")
                .description("Total number of failed password resets")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter tokenValidationCounter() {
        return Counter.builder("auth.token.validation")
                .description("Total number of token validations")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter tokenRefreshCounter() {
        return Counter.builder("auth.token.refresh")
                .description("Total number of token refreshes")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter accountLockoutCounter() {
        return Counter.builder("auth.account.lockouts")
                .description("Total number of account lockouts")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter emailSentCounter() {
        return Counter.builder("auth.emails.sent")
                .description("Total number of emails sent")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter emailFailedCounter() {
        return Counter.builder("auth.emails.failed")
                .description("Total number of emails that failed to send")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageSentCounter() {
        return Counter.builder("auth.kafka.messages.sent")
                .description("Total number of Kafka messages sent")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageFailedCounter() {
        return Counter.builder("auth.kafka.messages.failed")
                .description("Total number of Kafka messages that failed to send")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer loginProcessingTimer() {
        return Timer.builder("auth.login.processing.time")
                .description("Time taken to process login requests")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer registrationProcessingTimer() {
        return Timer.builder("auth.registration.processing.time")
                .description("Time taken to process registration requests")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer tokenValidationTimer() {
        return Timer.builder("auth.token.validation.time")
                .description("Time taken to validate tokens")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer emailSendingTimer() {
        return Timer.builder("auth.emails.sending.time")
                .description("Time taken to send emails")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter circuitBreakerOpenCounter() {
        return Counter.builder("auth.circuit.breaker.open")
                .description("Total number of circuit breaker open events")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter retryAttemptsCounter() {
        return Counter.builder("auth.retry.attempts")
                .description("Total number of retry attempts")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter rateLimitExceededCounter() {
        return Counter.builder("auth.rate.limit.exceeded")
                .description("Total number of rate limit exceeded events")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter bulkheadRejectedCounter() {
        return Counter.builder("auth.bulkhead.rejected")
                .description("Total number of bulkhead rejected calls")
                .tag("service", "auth-service")
                .register(meterRegistry);
    }
}
