package com.microservices.auth.repository;

import com.microservices.auth.entity.User;
import com.microservices.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionToken(String sessionToken);

    List<UserSession> findByUserAndActiveTrue(User user);

    List<UserSession> findByUserAndDeviceIdAndActiveTrue(User user, String deviceId);

    @Modifying
    @Query("UPDATE UserSession us SET us.active = false, us.loggedOutAt = :now WHERE us.user = :user")
    void deactivateAllUserSessions(User user, LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession us SET us.active = false, us.loggedOutAt = :now WHERE us.expiresAt < :now")
    void deactivateExpiredSessions(LocalDateTime now);

    @Query("SELECT COUNT(us) FROM UserSession us WHERE us.user = :user AND us.active = true")
    long countActiveSessionsByUser(User user);
}