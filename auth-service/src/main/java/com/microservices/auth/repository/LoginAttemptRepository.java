package com.microservices.auth.repository;

import com.microservices.auth.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing login attempts.
 */
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    /**
     * Find all login attempts by IP address
     */
    List<LoginAttempt> findByIpAddressOrderByCreatedAtDesc(String ipAddress);

    /**
     * Count failed login attempts by IP address within a time window
     */
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.success = false AND la.createdAt >= :since")
    long countFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    /**
     * Find recent failed login attempts by IP address
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.success = false AND la.createdAt >= :since ORDER BY la.createdAt DESC")
    List<LoginAttempt> findRecentFailedAttemptsByIp(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    /**
     * Find login attempts by username and IP address
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.username = :username AND la.ipAddress = :ipAddress ORDER BY la.createdAt DESC")
    List<LoginAttempt> findByUsernameAndIpAddress(@Param("username") String username, @Param("ipAddress") String ipAddress);

    /**
     * Delete expired login attempts
     */
    @Modifying
    @Query("DELETE FROM LoginAttempt la WHERE la.expiresAt < :now")
    int deleteExpiredAttempts(@Param("now") LocalDateTime now);

    /**
     * Count total login attempts by IP address
     */
    long countByIpAddress(String ipAddress);

    /**
     * Find last successful login attempt by IP address
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.success = true ORDER BY la.createdAt DESC")
    List<LoginAttempt> findLastSuccessfulAttemptByIp(@Param("ipAddress") String ipAddress);
}
