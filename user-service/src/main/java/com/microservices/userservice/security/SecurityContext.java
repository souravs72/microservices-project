package com.microservices.userservice.security;

import lombok.Data;

@Data
public class SecurityContext {
    private String username;
    private String role;
    private String ipAddress;

    private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();

    public static void setContext(SecurityContext context) {
        contextHolder.set(context);
    }

    public static SecurityContext getContext() {
        SecurityContext context = contextHolder.get();
        if (context == null) {
            context = new SecurityContext();
            contextHolder.set(context);
        }
        return context;
    }

    public static void clear() {
        contextHolder.remove();
    }
}