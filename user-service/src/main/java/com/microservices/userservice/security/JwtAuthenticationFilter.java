package com.microservices.userservice.security;

import com.microservices.userservice.dto.ValidateTokenRequest;
import com.microservices.userservice.dto.ValidateTokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${auth.service.validate-endpoint}")
    private String validateEndpoint;

    /**
     * Determine if this filter should not be applied to the current request.
     * Returns true for public endpoints that don't require authentication.
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
        
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);
            String ipAddress = getClientIpAddress(request);

            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                ValidateTokenResponse validationResponse = validateToken(jwt);

                if (validationResponse != null && validationResponse.isValid()) {
                    // Create authentication token with proper authorities
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    validationResponse.getUsername(),
                                    null,
                                    Collections.singletonList(
                                            new SimpleGrantedAuthority("ROLE_" + validationResponse.getRole())
                                    )
                            );
                    
                    // Set authentication details from request
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Set authentication in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Set custom security context with additional information
                    SecurityContext securityContext = new SecurityContext();
                    securityContext.setUsername(validationResponse.getUsername());
                    securityContext.setRole(validationResponse.getRole());
                    securityContext.setIpAddress(ipAddress);
                    SecurityContext.setContext(securityContext);

                    log.debug("User {} authenticated successfully with role {} from IP {}", 
                            validationResponse.getUsername(), 
                            validationResponse.getRole(),
                            ipAddress);
                } else {
                    log.debug("Token validation failed for request from IP {}", ipAddress);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Don't throw exception - let the request proceed to be handled by authentication entry point
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear custom security context after request processing
            SecurityContext.clear();
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Validate JWT token by calling Auth Service
     */
    private ValidateTokenResponse validateToken(String token) {
        try {
            String url = authServiceUrl + validateEndpoint;
            ValidateTokenRequest request = new ValidateTokenRequest(token);
            
            ValidateTokenResponse response = restTemplate.postForObject(
                    url, 
                    request, 
                    ValidateTokenResponse.class
            );
            
            return response;
        } catch (Exception e) {
            log.error("Error validating token with Auth Service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract client IP address from request, checking common proxy headers
     */
    @NonNull
    private String getClientIpAddress(HttpServletRequest request) {
        // Check common proxy headers in order of preference
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
                // X-Forwarded-For can contain multiple IPs, get the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}