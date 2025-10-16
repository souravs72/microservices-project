package com.microservices.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_device_id", columnList = "deviceId"),
        @Index(name = "idx_session_token", columnList = "sessionToken")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String sessionToken;

    @Column(length = 100)
    private String deviceId;

    @Column(length = 200)
    private String deviceName;

    @Column(length = 100)
    private String deviceType;  // MOBILE, WEB, DESKTOP

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastAccessedAt = LocalDateTime.now();

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean active = true;

    @Column
    private LocalDateTime loggedOutAt;
}