package com.microservices.orderservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    
    @Value("${app.security.internal-api-key}")
    private String internalApiKey;
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Internal-API-Key", internalApiKey);
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}

