package com.microservices.auth.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Allow all actuator health and info endpoints
                        .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                        // Allow actuator base path (so /actuator/health works even if matched partially)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Everything else actuator-related requires ADMIN
                        .anyRequest().hasRole("ADMIN")
                );

        return http.build();
    }
}