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
import org.springframework.http.HttpMethod;
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

    @Value("${notification-service.url:http://notification-service:8085}")
    private String notificationServiceUrl;

    @Value("${order-service.url:http://order-service:8083}")
    private String orderServiceUrl;

    @Value("${inventory-service.url:http://inventory-service:8084}")
    private String inventoryServiceUrl;

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
                // User Service Swagger (no auth)
                // ==========================
                .route("user-service-swagger", r -> r
                        .path("/user-swagger/**", "/user-api-docs/**")
                        .filters(f -> f
                                .rewritePath("/user-swagger/(?<path>.*)", "/swagger-ui/${path}")
                                .rewritePath("/user-api-docs/(?<path>.*)", "/api-docs/${path}"))
                        .uri(userServiceUrl))

                // ==========================
                // Auth Service Swagger (no auth)
                // ==========================
                .route("auth-service-swagger", r -> r
                        .path("/auth-swagger/**", "/auth-api-docs/**")
                        .filters(f -> f
                                .rewritePath("/auth-swagger/(?<path>.*)", "/swagger-ui/${path}")
                                .rewritePath("/auth-api-docs/(?<path>.*)", "/api-docs/${path}"))
                        .uri(authServiceUrl))

                // ==========================
                // Notification Service Swagger (no auth)
                // ==========================
                .route("notification-service-swagger", r -> r
                        .path("/notification-swagger/**", "/notification-api-docs/**")
                        .filters(f -> f
                                .rewritePath("/notification-swagger/(?<path>.*)", "/swagger-ui/${path}")
                                .rewritePath("/notification-api-docs/(?<path>.*)", "/api-docs/${path}"))
                        .uri(notificationServiceUrl))

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
                // User Service - User Creation (no auth required)
                // ==========================
                .route("user-service-create", r -> r
                        .path("/api/users")
                        .and()
                        .method(HttpMethod.POST)
                        .filters(f -> f
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(500), 2, true))
                                .circuitBreaker(config -> config
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/users"))
                                .addRequestHeader("X-Gateway-Service", "api-gateway"))
                        .uri(userServiceUrl))

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

                // ==========================
                // Order Service (requires auth)
                // ==========================
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(500), 2, true))
                                .circuitBreaker(config -> config
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .addRequestHeader("X-Gateway-Service", "api-gateway"))
                        .uri(orderServiceUrl))

                // ==========================
                // Inventory Service (requires auth)
                // ==========================
                .route("inventory-service", r -> r
                        .path("/api/inventory/**", "/api/products/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(500), 2, true))
                                .circuitBreaker(config -> config
                                        .setName("inventoryServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/inventory"))
                                .addRequestHeader("X-Gateway-Service", "api-gateway"))
                        .uri(inventoryServiceUrl))

                       // ==========================
                       // Notification Service (requires JWT auth)
                       // ==========================
                       .route("notification-service", r -> r
                               .path("/api/notifications", "/api/notifications/**")
                               .filters(f -> f
                                       .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                       .rewritePath("/api/notifications$", "/notifications")
                                       .rewritePath("/api/notifications/(?<segment>.*)", "/notifications/${segment}")
                                       .retry(config -> config
                                               .setRetries(2)
                                               .setBackoff(Duration.ofMillis(100), Duration.ofMillis(500), 2, true))
                                       .circuitBreaker(config -> config
                                               .setName("notificationServiceCircuitBreaker")
                                               .setFallbackUri("forward:/fallback/notifications"))
                                       .addRequestHeader("X-Gateway-Service", "api-gateway"))
                               .uri(notificationServiceUrl))

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
