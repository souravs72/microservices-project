package com.microservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String email;
    private String role;
    private Long userId;

    public AuthResponse(String accessToken, String refreshToken, String type,
                        String username, String email, String role, Long userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = type;
        this.username = username;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }
}