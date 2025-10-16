package com.microservices.userservice.config;

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
    public Counter userCreationCounter() {
        return Counter.builder("user.creation")
                .description("Total number of user creations")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter userUpdateCounter() {
        return Counter.builder("user.update")
                .description("Total number of user updates")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter userDeletionCounter() {
        return Counter.builder("user.deletion")
                .description("Total number of user deletions")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter userRetrievalCounter() {
        return Counter.builder("user.retrieval")
                .description("Total number of user retrievals")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter graphqlQueryCounter() {
        return Counter.builder("graphql.queries")
                .description("Total number of GraphQL queries")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter graphqlMutationCounter() {
        return Counter.builder("graphql.mutations")
                .description("Total number of GraphQL mutations")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter restApiCallCounter() {
        return Counter.builder("rest.api.calls")
                .description("Total number of REST API calls")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter authValidationCounter() {
        return Counter.builder("auth.validation")
                .description("Total number of auth validations")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter authValidationSuccessCounter() {
        return Counter.builder("auth.validation.success")
                .description("Total number of successful auth validations")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter authValidationFailureCounter() {
        return Counter.builder("auth.validation.failure")
                .description("Total number of failed auth validations")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageConsumedCounter() {
        return Counter.builder("kafka.messages.consumed")
                .description("Total number of Kafka messages consumed")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageProcessedCounter() {
        return Counter.builder("kafka.messages.processed")
                .description("Total number of Kafka messages processed successfully")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaMessageFailedCounter() {
        return Counter.builder("kafka.messages.failed")
                .description("Total number of Kafka messages that failed processing")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer userCreationTimer() {
        return Timer.builder("user.creation.time")
                .description("Time taken to create users")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer userUpdateTimer() {
        return Timer.builder("user.update.time")
                .description("Time taken to update users")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer userRetrievalTimer() {
        return Timer.builder("user.retrieval.time")
                .description("Time taken to retrieve users")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer graphqlQueryTimer() {
        return Timer.builder("graphql.query.time")
                .description("Time taken to process GraphQL queries")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer graphqlMutationTimer() {
        return Timer.builder("graphql.mutation.time")
                .description("Time taken to process GraphQL mutations")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer authValidationTimer() {
        return Timer.builder("auth.validation.time")
                .description("Time taken to validate auth tokens")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter circuitBreakerOpenCounter() {
        return Counter.builder("circuit.breaker.open")
                .description("Total number of circuit breaker open events")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter retryAttemptsCounter() {
        return Counter.builder("retry.attempts")
                .description("Total number of retry attempts")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter rateLimitExceededCounter() {
        return Counter.builder("rate.limit.exceeded")
                .description("Total number of rate limit exceeded events")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter bulkheadRejectedCounter() {
        return Counter.builder("bulkhead.rejected")
                .description("Total number of bulkhead rejected calls")
                .tag("service", "user-service")
                .register(meterRegistry);
    }
}
