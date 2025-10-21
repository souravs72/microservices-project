package com.microservices.userservice.config;

import com.microservices.userservice.entity.User;
import com.microservices.userservice.security.SecurityContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class IpAddressAuditListener {

    private String getCurrentIp() {
        SecurityContext context = SecurityContext.getContext();
        return (context != null) ? context.getIpAddress() : null;
    }

    @PrePersist
    public void setCreatedFromIp(User entity) {
        String ip = getCurrentIp();
        if (ip != null) {
            entity.setCreatedFromIp(ip);
        }
    }

    @PreUpdate
    public void setUpdatedFromIp(User entity) {
        String ip = getCurrentIp();
        if (ip != null) {
            entity.setUpdatedFromIp(ip);
        }
    }
}