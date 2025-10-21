package com.microservices.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_processed", columnList = "processed"),
        @Index(name = "idx_event_type", columnList = "eventType"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String aggregateType;  // USER, ORDER, etc.

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false, length = 50)
    private String eventType;  // USER_CREATED, USER_UPDATED, etc.

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean processed = false;

    @Column
    private LocalDateTime processedAt;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column
    private LocalDateTime lastRetryAt;

    @Column
    private LocalDateTime nextRetryAt;

    @Column(nullable = false)
    private Boolean failed = false;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(length = 50)
    private String correlationId;
}