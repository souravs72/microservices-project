package com.microservices.apigateway.config;

import com.microservices.apigateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Value("${auth-service.url:http://auth-service:8082}")
    private String authServiceUrl;

    @Value("${user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    /**
     * Defines all routes for API Gateway
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ==========================
                // Health Check (no auth)
                // ==========================
                .route("gateway-health", r -> r
                        .path("/health", "/actuator/health")
                        .filters(f -> f.setStatus(HttpStatus.OK))
                        .uri("no://op"))

                // ==========================
                // Auth Service (no auth)
                // ==========================
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .retry(config -> config
                                        .setRetries(3)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                                .circuitBreaker(config -> config
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                                .addRequestHeader("X-Gateway-Service", "api-gateway"))
                        .uri(authServiceUrl))

                // ==========================
                // User Service (REST, requires auth)
                // ==========================
                .route("user-service-rest", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(500), 2, true))
                                .circuitBreaker(config -> config
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/users"))
                                .addRequestHeader("X-Gateway-Service", "api-gateway"))
                        .uri(userServiceUrl))

                // ==========================
                // User Service (GraphQL, requires auth)
                // ==========================
                .route("user-service-graphql", r -> r
                        .path("/graphql", "/playground")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("userServiceGraphQLCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/graphql"))
                                .addRequestHeader("X-Gateway-Service", "api-gateway"))
                        .uri(userServiceUrl))

                .build();
    }

    // ==========================
    // Redis Rate Limiting Config
    // ==========================
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // replenishRate = 10 requests/sec, burstCapacity = 20, requestedTokens = 1
        return new RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        // Use "X-User-Username" header as rate-limiting key, fallback to "anonymous"
        return exchange -> {
            String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
            return Mono.just(username != null ? username : "anonymous");
        };
    }

    // ==========================
    // Global Filter for Response Time
    // ==========================
    @Bean
    public GlobalFilter responseTimeFilter() {
        return (exchange, chain) -> {
            long startTime = System.currentTimeMillis();
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                exchange.getResponse().getHeaders().add("X-Response-Time", duration + "ms");
            }));
        };
    }
}
