package com.microservices.auth.dto;

import lombok.Data;

@Data
public class ValidateTokenRequest {
    private String token;
}
