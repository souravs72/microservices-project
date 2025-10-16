package com.microservices.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
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

    @Value("${app.security.internal-api-key}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // Only apply to internal sync endpoint
        if (requestUri.equals("/api/users/sync")) {
            String apiKey = request.getHeader("X-Internal-API-Key");

            if (apiKey == null || !apiKey.equals(internalApiKey)) {
                log.warn("Invalid or missing internal API key for sync endpoint from IP: {}",
                        request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Invalid API key\"}");
                return;
            }

            // Set internal service authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "internal-service",
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Internal service authenticated for sync endpoint");
        }

        filterChain.doFilter(request, response);
    }
}