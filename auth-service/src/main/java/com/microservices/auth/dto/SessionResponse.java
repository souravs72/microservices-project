package com.microservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SessionResponse {
    private Long id;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private Boolean active;
}