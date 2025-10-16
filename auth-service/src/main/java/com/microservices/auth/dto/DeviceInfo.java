package com.microservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {
    private String deviceId;
    private String deviceName;
    private String deviceType;  // MOBILE, WEB, DESKTOP
    private String userAgent;
}