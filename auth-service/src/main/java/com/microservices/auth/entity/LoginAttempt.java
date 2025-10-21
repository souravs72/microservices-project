package com.microservices.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to track login attempts by IP address for security purposes.
 * Used to implement IP-based rate limiting and account locking.
 */
@Entity
@Table(name = "login_attempts", 
       indexes = {
           @Index(name = "idx_login_attempts_ip", columnList = "ip_address"),
           @Index(name = "idx_login_attempts_username", columnList = "username"),
           @Index(name = "idx_login_attempts_created_at", columnList = "created_at"),
           @Index(name = "idx_login_attempts_success", columnList = "success")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "IP address is required")
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "username", length = 50)
    private String username;

    @NotNull(message = "Success status is required")
    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Constructor for successful login
    public LoginAttempt(String ipAddress, String username, String userAgent) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.success = true;
        this.userAgent = userAgent;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(30); // Keep successful attempts for 30 days
    }

    // Constructor for failed login
    public LoginAttempt(String ipAddress, String username, String failureReason, String userAgent) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.success = false;
        this.failureReason = failureReason;
        this.userAgent = userAgent;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(1); // Keep failed attempts for 1 day
    }

    /**
     * Check if this login attempt has expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
