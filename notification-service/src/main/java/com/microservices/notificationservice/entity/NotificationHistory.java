package com.microservices.notificationservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Notification history entity for tracking all notification events.
 * Includes comprehensive audit trail and validation.
 */
@Entity
@Table(name = "notification_history",
       indexes = {
           @Index(name = "idx_notification_history_event_id", columnList = "event_id"),
           @Index(name = "idx_notification_history_event_type", columnList = "event_type"),
           @Index(name = "idx_notification_history_recipient_email", columnList = "recipient_email"),
           @Index(name = "idx_notification_history_status", columnList = "status"),
           @Index(name = "idx_notification_history_created_at", columnList = "created_at"),
           @Index(name = "idx_notification_history_sent_at", columnList = "sent_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_notification_history_event_id", columnNames = "event_id")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Event ID is required")
    @Size(max = 255, message = "Event ID must not exceed 255 characters")
    @Column(name = "event_id", unique = true, nullable = false, length = 255)
    private String eventId;

    @NotBlank(message = "Event type is required")
    @Size(max = 100, message = "Event type must not exceed 100 characters")
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email should be valid")
    @Size(max = 255, message = "Recipient email must not exceed 255 characters")
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Size(max = 255, message = "Recipient name must not exceed 255 characters")
    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    @Size(max = 500, message = "Subject must not exceed 500 characters")
    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Size(max = 100, message = "Template name must not exceed 100 characters")
    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "template_variables", columnDefinition = "TEXT")
    private String templateVariables;

    @Size(max = 100, message = "Channel must not exceed 100 characters")
    @Column(name = "channel", length = 100)
    private String channel = "EMAIL";

    @Size(max = 100, message = "Priority must not exceed 100 characters")
    @Column(name = "priority", length = 100)
    private String priority = "NORMAL";

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "user_id")
    private Long userId;

    @Size(max = 255, message = "External reference must not exceed 255 characters")
    @Column(name = "external_reference", length = 255)
    private String externalReference;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

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

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if notification can be retried
     */
    public boolean canRetry() {
        return status == NotificationStatus.FAILED && 
               retryCount < maxRetries && 
               (nextRetryAt == null || nextRetryAt.isBefore(LocalDateTime.now()));
    }

    /**
     * Check if notification is expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if notification is scheduled for future
     */
    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }

    /**
     * Check if notification should be processed now
     */
    public boolean shouldProcessNow() {
        return !isExpired() && 
               (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now())) &&
               status == NotificationStatus.PENDING;
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
        
        if (this.retryCount >= this.maxRetries) {
            this.status = NotificationStatus.FAILED;
            this.failedAt = LocalDateTime.now();
        } else {
            this.status = NotificationStatus.RETRYING;
            this.nextRetryAt = LocalDateTime.now().plusMinutes(5 * this.retryCount); // Exponential backoff
        }
    }

    /**
     * Mark as sent
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Notification status enum
     */
    public enum NotificationStatus {
        PENDING("Pending"),
        SENT("Sent"),
        DELIVERED("Delivered"),
        FAILED("Failed"),
        RETRYING("Retrying"),
        CANCELLED("Cancelled");

        private final String displayName;

        NotificationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
