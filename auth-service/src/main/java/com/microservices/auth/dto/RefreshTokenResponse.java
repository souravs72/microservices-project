package com.microservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private Long expiresIn;  // in seconds
}