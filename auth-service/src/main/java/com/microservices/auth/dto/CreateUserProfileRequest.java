package com.microservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserProfileRequest {
    private Long userId;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
