package com.microservices.userservice.config;

import com.microservices.userservice.entity.base.AuditableEntity;
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
            entity.setCreatedFromIp(ip);
            entity.setModifiedFromIp(ip); // initialize both on create
        }
    }

    @PreUpdate
    public void setModifiedFromIp(AuditableEntity entity) {
        String ip = getCurrentIp();
        if (ip != null) {
            entity.setModifiedFromIp(ip);
        }
    }
}
