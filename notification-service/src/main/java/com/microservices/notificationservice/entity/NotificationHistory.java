package com.microservices.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_history", indexes = {
        @Index(name = "idx_event_id", columnList = "eventId"),
        @Index(name = "idx_recipient_email", columnList = "recipientEmail"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String notificationType;  // EMAIL, SMS, PUSH

    @Column(nullable = false)
    private String recipientEmail;

    @Column
    private String recipientName;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private String status;  // PENDING, SENT, FAILED

    @Column
    private String errorMessage;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime sentAt;

    @Column(length = 50)
    private String correlationId;
}