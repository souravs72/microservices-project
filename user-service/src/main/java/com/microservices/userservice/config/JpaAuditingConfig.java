package com.microservices.userservice.config;

import com.microservices.userservice.security.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            SecurityContext context = SecurityContext.getContext();
            if (context != null && context.getUsername() != null) {
                return Optional.of(context.getUsername());
            }
            return Optional.of("system"); // fallback
        };
    }
}
