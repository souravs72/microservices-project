package com.microservices.auth.repository;

import com.microservices.auth.entity.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.processed = false ORDER BY oe.createdAt ASC")
    List<OutboxEvent> findUnprocessedEvents(Pageable pageable);

    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.processed = false ORDER BY oe.createdAt ASC")
    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();

    @Modifying
    @Query("DELETE FROM OutboxEvent oe WHERE oe.processed = true AND oe.processedAt < :cutoffDate")
    void deleteProcessedEventsOlderThan(LocalDateTime cutoffDate);

    @Modifying
    @Query("DELETE FROM OutboxEvent oe WHERE oe.processed = true AND oe.createdAt < :cutoffDate")
    int deleteByProcessedTrueAndCreatedAtBefore(LocalDateTime cutoffDate);

    @Query("SELECT COUNT(oe) FROM OutboxEvent oe WHERE oe.processed = false")
    long countUnprocessedEvents();

    long countByProcessedFalse();

    long countByFailedTrue();

    long countByRetryCountGreaterThan(int retryCount);

    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.processed = false AND oe.nextRetryAt <= :now ORDER BY oe.nextRetryAt ASC")
    List<OutboxEvent> findEventsReadyForRetry(LocalDateTime now);

    @Query("SELECT oe FROM OutboxEvent oe WHERE oe.processed = false AND oe.retryCount < :maxRetries ORDER BY oe.createdAt ASC")
    List<OutboxEvent> findEventsForRetry(int maxRetries);
}