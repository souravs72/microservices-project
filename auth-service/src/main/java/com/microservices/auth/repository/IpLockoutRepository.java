package com.microservices.auth.repository;

import com.microservices.auth.entity.IpLockout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing IP lockouts.
 */
@Repository
public interface IpLockoutRepository extends JpaRepository<IpLockout, Long> {

    /**
     * Find IP lockout by IP address
     */
    Optional<IpLockout> findByIpAddress(String ipAddress);

    /**
     * Check if IP is currently locked
     */
    @Query("SELECT CASE WHEN COUNT(il) > 0 THEN true ELSE false END FROM IpLockout il WHERE il.ipAddress = :ipAddress AND il.lockedUntil > :now")
    boolean isIpLocked(@Param("ipAddress") String ipAddress, @Param("now") LocalDateTime now);

    /**
     * Find all currently locked IPs
     */
    @Query("SELECT il FROM IpLockout il WHERE il.lockedUntil > :now ORDER BY il.lockedUntil ASC")
    List<IpLockout> findCurrentlyLockedIps(@Param("now") LocalDateTime now);

    /**
     * Find expired lockouts
     */
    @Query("SELECT il FROM IpLockout il WHERE il.lockedUntil IS NOT NULL AND il.lockedUntil <= :now")
    List<IpLockout> findExpiredLockouts(@Param("now") LocalDateTime now);

    /**
     * Delete expired lockouts
     */
    @Modifying
    @Query("DELETE FROM IpLockout il WHERE il.lockedUntil IS NOT NULL AND il.lockedUntil <= :now")
    int deleteExpiredLockouts(@Param("now") LocalDateTime now);

    /**
     * Count total lockouts by IP address
     */
    long countByIpAddress(String ipAddress);

    /**
     * Find lockouts created within a time window
     */
    @Query("SELECT il FROM IpLockout il WHERE il.ipAddress = :ipAddress AND il.createdAt >= :since ORDER BY il.createdAt DESC")
    List<IpLockout> findLockoutsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
}
