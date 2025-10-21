package com.microservices.userservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.microservices.userservice.entity.User.UserRole;

@Data
@NoArgsConstructor
public class UserDTO {
    public UserDTO(Long id, String username, String email, String firstName, String lastName, String phone,
            String address, String bio, String profilePictureUrl, UserRole role, LocalDateTime memberSince,
            Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String updatedBy) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.role = role != null ? role.toString() : null;
        this.memberSince = memberSince;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String bio;
    private String profilePictureUrl;
    private String role;
    private LocalDateTime memberSince;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
