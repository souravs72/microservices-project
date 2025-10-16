package com.microservices.notificationservice.config;

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
    public Counter notificationProcessedCounter() {
        return Counter.builder("notifications.processed")
                .description("Total number of notifications processed")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter notificationSuccessCounter() {
        return Counter.builder("notifications.success")
                .description("Total number of successful notifications")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter notificationFailureCounter() {
        return Counter.builder("notifications.failure")
                .description("Total number of failed notifications")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageConsumedCounter() {
        return Counter.builder("kafka.messages.consumed")
                .description("Total number of Kafka messages consumed")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageProcessedCounter() {
        return Counter.builder("kafka.messages.processed")
                .description("Total number of Kafka messages processed successfully")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageFailedCounter() {
        return Counter.builder("kafka.messages.failed")
                .description("Total number of Kafka messages that failed processing")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter emailSentCounter() {
        return Counter.builder("emails.sent")
                .description("Total number of emails sent")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter emailFailedCounter() {
        return Counter.builder("emails.failed")
                .description("Total number of emails that failed to send")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer notificationProcessingTimer() {
        return Timer.builder("notifications.processing.time")
                .description("Time taken to process notifications")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer emailSendingTimer() {
        return Timer.builder("emails.sending.time")
                .description("Time taken to send emails")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter idempotencyCacheHitCounter() {
        return Counter.builder("idempotency.cache.hit")
                .description("Total number of idempotency cache hits")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter idempotencyCacheMissCounter() {
        return Counter.builder("idempotency.cache.miss")
                .description("Total number of idempotency cache misses")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter circuitBreakerOpenCounter() {
        return Counter.builder("circuit.breaker.open")
                .description("Total number of circuit breaker open events")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter retryAttemptsCounter() {
        return Counter.builder("retry.attempts")
                .description("Total number of retry attempts")
                .tag("service", "notification-service")
                .register(meterRegistry);
    }
}
