package com.microservices.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * User entity for authentication service.
 * Contains authentication-specific fields and security tracking.
 */
@Entity
@Table(name = "auth_users", 
       indexes = {
           @Index(name = "idx_auth_users_username", columnList = "username"),
           @Index(name = "idx_auth_users_email", columnList = "email"),
           @Index(name = "idx_auth_users_enabled", columnList = "enabled"),
           @Index(name = "idx_auth_users_account_locked", columnList = "account_locked"),
           @Index(name = "idx_auth_users_created_at", columnList = "created_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_auth_users_username", columnNames = "username"),
           @UniqueConstraint(name = "uk_auth_users_email", columnNames = "email")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores, and hyphens")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @NotNull(message = "Enabled status is required")
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_from_ip", length = 45, updatable = false)
    private String createdFromIp;

    @Column(name = "updated_from_ip", length = 45)
    private String updatedFromIp;

    // Security fields
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;

    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;

    @Column(name = "account_locked_at")
    private LocalDateTime accountLockedAt;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "email_verification_expires_at")
    private LocalDateTime emailVerificationExpiresAt;

    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @Column(name = "two_factor_enabled", nullable = false)
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret", length = 255)
    private String twoFactorSecret;

    @Column(name = "backup_codes", length = 1000)
    private String backupCodes;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (passwordChangedAt == null) {
            passwordChangedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the account is locked
     */
    public boolean isAccountLocked() {
        return accountLocked || (accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now()));
    }

    /**
     * Check if the account is not locked
     */
    public boolean isAccountNonLocked() {
        return enabled && !isAccountLocked();
    }

    /**
     * Check if email verification token is valid
     */
    public boolean isEmailVerificationTokenValid() {
        return emailVerificationToken != null && 
               emailVerificationExpiresAt != null && 
               emailVerificationExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Check if password reset token is valid
     */
    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null && 
               passwordResetExpiresAt != null && 
               passwordResetExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        this.lastFailedLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Lock account after 5 failed attempts
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.accountLockedAt = LocalDateTime.now();
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30); // Lock for 30 minutes
        }
    }

    /**
     * Reset failed login attempts (on successful login)
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.accountLockedAt = null;
        this.accountLockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * User roles enum
     */
    public enum UserRole {
        USER("User"),
        ADMIN("Administrator"),
        MODERATOR("Moderator"),
        SUPPORT("Support");

        private final String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}