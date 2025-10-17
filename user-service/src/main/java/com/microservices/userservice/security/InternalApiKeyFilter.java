package com.microservices.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Order(2)
@Slf4j
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-API-Key";
    private static final String SYNC_ENDPOINT = "/api/users/sync";

    @Value("${app.security.internal-api-key}")
    private String internalApiKey;

    /**
     * Determine if this filter should not be applied to the current request.
     * This filter only applies to the internal sync endpoint.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip filter for Swagger/OpenAPI documentation endpoints
        if (path.startsWith("/swagger-ui") || 
            path.equals("/swagger-ui.html") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/api-docs") ||
            path.equals("/api-docs")) {
            return true;
        }
        
        // Skip filter for public actuator endpoints
        if (path.equals("/actuator/health") || 
            path.equals("/actuator/info")) {
            return true;
        }
        
        // Skip filter for H2 console (dev only)
        if (path.startsWith("/h2-console")) {
            return true;
        }
        
        // Skip filter for GraphiQL (dev only)
        if (path.startsWith("/graphiql")) {
            return true;
        }
        
        // Only apply this filter to the sync endpoint
        return !path.equals(SYNC_ENDPOINT);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        
        // This should only be called for the sync endpoint due to shouldNotFilter
        if (requestUri.equals(SYNC_ENDPOINT)) {
            String apiKey = request.getHeader(INTERNAL_API_KEY_HEADER);

            // Validate API key
            if (!isValidApiKey(apiKey)) {
                log.warn("Invalid or missing internal API key for sync endpoint. " +
                        "Request from IP: {}, User-Agent: {}", 
                        getClientIpAddress(request),
                        request.getHeader("User-Agent"));
                
                sendForbiddenResponse(response);
                return;
            }

            // Set internal service authentication
            authenticateInternalService(request);
            
            log.debug("Internal service authenticated successfully for sync endpoint");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Validate the provided API key
     */
    private boolean isValidApiKey(String apiKey) {
        return apiKey != null && 
               !apiKey.isEmpty() && 
               apiKey.equals(internalApiKey);
    }

    /**
     * Set authentication for internal service in Security Context
     */
    private void authenticateInternalService(HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Send 403 Forbidden response with JSON error message
     */
    private void sendForbiddenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
                "{\"error\":\"Forbidden\",\"message\":\"Invalid or missing API key\",\"timestamp\":\"%s\"}",
                java.time.LocalDateTime.now().toString()
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * Extract client IP address from request, checking common proxy headers
     */
    @NonNull
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_CLIENT_IP"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}