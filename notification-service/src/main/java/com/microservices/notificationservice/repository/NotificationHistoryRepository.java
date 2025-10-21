package com.microservices.notificationservice.repository;

import com.microservices.notificationservice.entity.NotificationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    Optional<NotificationHistory> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<NotificationHistory> findByStatus(NotificationHistory.NotificationStatus status);

    List<NotificationHistory> findByRecipientEmail(String recipientEmail);

    Page<NotificationHistory> findByRecipientEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<NotificationHistory> findByStatus(NotificationHistory.NotificationStatus status, Pageable pageable);

    @Query("SELECT nh FROM NotificationHistory nh WHERE nh.status = 'FAILED' AND nh.retryCount < nh.maxRetries")
    List<NotificationHistory> findFailedNotificationsForRetry();

    @Query("SELECT COUNT(nh) FROM NotificationHistory nh WHERE nh.createdAt > :since AND nh.status = 'SENT'")
    long countSentNotificationsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(nh) FROM NotificationHistory nh WHERE nh.createdAt > :since AND nh.status = 'FAILED'")
    long countFailedNotificationsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(nh) FROM NotificationHistory nh WHERE nh.status = 'PENDING'")
    long countPendingNotifications();
}
