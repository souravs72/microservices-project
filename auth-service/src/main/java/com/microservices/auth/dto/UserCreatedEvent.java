package com.microservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String eventType = "USER_CREATED";
    private Long timestamp;
}



