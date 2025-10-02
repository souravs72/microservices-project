package com.microservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateTokenResponse {
    private boolean valid;
    private String username;
    private String role;
}
