package com.microservices.notificationservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${internal.api.key}")
    private String internalApiKey;

    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-API-Key";
    private static final String SERVICE_ROLE = "ROLE_SERVICE";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        
        // Only apply this filter to internal API endpoints
        if (requestPath.startsWith("/api/internal/")) {
            String apiKey = request.getHeader(INTERNAL_API_KEY_HEADER);
            
            if (apiKey != null && apiKey.equals(internalApiKey)) {
                log.debug("Valid internal API key provided for path: {}", requestPath);
                
                // Set authentication for internal service
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "internal-service",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(SERVICE_ROLE))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                log.warn("Invalid or missing internal API key for path: {}", requestPath);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid internal API key\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
