package com.microservices.userservice.config;

import com.microservices.common.entity.AuditableEntity;
import com.microservices.userservice.security.SecurityContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class IpAddressAuditListener {

    private String getCurrentIp() {
        SecurityContext context = SecurityContext.getContext();
        return (context != null) ? context.getIpAddress() : null;
    }

    @PrePersist
    public void setCreatedFromIp(AuditableEntity entity) {
        String ip = getCurrentIp();
        if (ip != null) {
            // Note: Common AuditableEntity doesn't have IP fields
            // This listener is kept for compatibility but won't set IP fields
        }
    }

    @PreUpdate
    public void setUpdatedFromIp(AuditableEntity entity) {
        String ip = getCurrentIp();
        if (ip != null) {
            // Note: Common AuditableEntity doesn't have IP fields
            // This listener is kept for compatibility but won't set IP fields
        }
    }
}
