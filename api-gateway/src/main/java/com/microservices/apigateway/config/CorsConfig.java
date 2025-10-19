package com.microservices.apigateway.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {
    // Removed custom CorsWebFilter. Using spring.cloud.gateway.server.webflux.cors global configuration instead.
}