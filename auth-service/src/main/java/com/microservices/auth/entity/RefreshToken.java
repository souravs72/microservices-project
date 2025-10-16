package com.microservices.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_device_id", columnList = "deviceId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean revoked = false;

    @Column
    private LocalDateTime revokedAt;

    // Device tracking
    @Column(length = 100)
    private String deviceId;

    @Column(length = 200)
    private String deviceName;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}