package com.microservices.apigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.apigateway.util.JwtUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit.capacity:100}")
    private int rateLimitCapacity;

    @Value("${rate-limit.refill-tokens:100}")
    private int rateLimitRefillTokens;

    @Value("${rate-limit.refill-duration-minutes:1}")
    private int rateLimitRefillDuration;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Simple in-memory rate limiter (consider Redis for production)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED, "MISSING_AUTH_HEADER");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header format. Expected: Bearer <token>",
                        HttpStatus.UNAUTHORIZED, "INVALID_AUTH_FORMAT");
            }

            String token = authHeader.substring(7).trim();

            if (token.isEmpty()) {
                return onError(exchange, "Token cannot be empty", HttpStatus.UNAUTHORIZED, "EMPTY_TOKEN");
            }

            try {
                // Validate token
                JwtUtil.ValidationResult validationResult = jwtUtil.validateTokenWithDetails(token);

                if (!validationResult.isValid()) {
                    log.warn("Token validation failed: {}", validationResult.getMessage());
                    return onError(exchange, validationResult.getMessage(), HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
                }

                // Extract user information
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                if (username == null || username.isEmpty()) {
                    return onError(exchange, "Invalid token: missing username", HttpStatus.UNAUTHORIZED, "MISSING_USERNAME");
                }

                // Rate limiting (optional)
                if (rateLimitEnabled && !checkRateLimit(username)) {
                    log.warn("Rate limit exceeded for user: {}", username);
                    return onError(exchange, "Rate limit exceeded. Please try again later.",
                            HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
                }

                // Add user information to request headers
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Username", username)
                        .header("X-User-Role", role != null ? role : "USER")
                        .header("X-Auth-Time", LocalDateTime.now().toString())
                        .build();

                log.debug("User {} with role {} accessed {} {}",
                        username, role, request.getMethod(), request.getURI());

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("Expired token attempt from IP: {}", getClientIp(request));
                return onError(exchange, "Token has expired. Please login again.",
                        HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                log.error("Malformed JWT token from IP: {}", getClientIp(request));
                return onError(exchange, "Invalid token format", HttpStatus.UNAUTHORIZED, "MALFORMED_TOKEN");
            } catch (io.jsonwebtoken.security.SignatureException e) {
                log.error("Invalid JWT signature from IP: {}", getClientIp(request));
                return onError(exchange, "Invalid token signature", HttpStatus.UNAUTHORIZED, "INVALID_SIGNATURE");
            } catch (Exception e) {
                log.error("JWT validation error from IP: {}: {}", getClientIp(request), e.getMessage(), e);
                return onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED, "AUTH_FAILED");
            }
        };
    }

    private boolean checkRateLimit(String username) {
        Bucket bucket = buckets.computeIfAbsent(username, k -> createNewBucket());
        return bucket.tryConsume(1);
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
                rateLimitCapacity,
                Refill.intervally(rateLimitRefillTokens, Duration.ofMinutes(rateLimitRefillDuration))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status, String errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("path", exchange.getRequest().getPath().value());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error creating error response", e);
            return response.setComplete();
        }
    }

    public static class Config {
        // Configuration properties
        private boolean requireRole;
        private String[] allowedRoles;

        public boolean isRequireRole() {
            return requireRole;
        }

        public void setRequireRole(boolean requireRole) {
            this.requireRole = requireRole;
        }

        public String[] getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(String[] allowedRoles) {
            this.allowedRoles = allowedRoles;
        }
    }
}