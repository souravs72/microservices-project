package com.microservices.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to track IP address lockouts due to multiple failed login attempts.
 * Implements IP-based rate limiting for security.
 */
@Entity
@Table(name = "ip_lockouts", 
       indexes = {
           @Index(name = "idx_ip_lockouts_ip", columnList = "ip_address"),
           @Index(name = "idx_ip_lockouts_locked_until", columnList = "locked_until"),
           @Index(name = "idx_ip_lockouts_created_at", columnList = "created_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_ip_lockouts_ip", columnNames = "ip_address")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpLockout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "IP address is required")
    @Column(name = "ip_address", unique = true, nullable = false, length = 45)
    private String ipAddress;

    @NotNull(message = "Failed attempts count is required")
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "lockout_reason", length = 255)
    private String lockoutReason;

    // Constructor for new IP lockout
    public IpLockout(String ipAddress) {
        this.ipAddress = ipAddress;
        this.failedAttempts = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the IP is currently locked
     */
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Check if the IP is not locked
     */
    public boolean isNotLocked() {
        return !isLocked();
    }

    /**
     * Increment failed attempts and lock if threshold reached
     */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
        this.lastAttemptAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        if (this.failedAttempts >= 3) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(15); // Lock for 15 minutes
            this.lockoutReason = "Too many failed login attempts";
        }
    }

    /**
     * Reset failed attempts (on successful login)
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lockedUntil = null;
        this.lockoutReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get remaining lockout time in minutes
     */
    public long getRemainingLockoutMinutes() {
        if (!isLocked()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), lockedUntil).toMinutes();
    }
}
