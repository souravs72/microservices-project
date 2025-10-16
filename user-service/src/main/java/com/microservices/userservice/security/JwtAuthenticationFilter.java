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
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    validationResponse.getUsername(),
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + validationResponse.getRole()))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Set security context
                    SecurityContext securityContext = new SecurityContext();
                    securityContext.setUsername(validationResponse.getUsername());
                    securityContext.setRole(validationResponse.getRole());
                    securityContext.setIpAddress(ipAddress);
                    SecurityContext.setContext(securityContext);

                    log.debug("User {} authenticated with role {}", validationResponse.getUsername(), validationResponse.getRole());
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);

        // Clear context
        SecurityContext.clear();
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private ValidateTokenResponse validateToken(String token) {
        try {
            String url = authServiceUrl + validateEndpoint;
            ValidateTokenRequest request = new ValidateTokenRequest(token);
            return restTemplate.postForObject(url, request, ValidateTokenResponse.class);
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return null;
        }
    }

    @NonNull
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR"
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