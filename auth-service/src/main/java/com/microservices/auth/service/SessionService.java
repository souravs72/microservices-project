package com.microservices.auth.service;

import com.microservices.auth.dto.SessionResponse;
import com.microservices.auth.entity.User;
import com.microservices.auth.entity.UserSession;
import com.microservices.auth.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final UserSessionRepository sessionRepository;

    @Value("${jwt.refresh-token-expiration}")
    private Long sessionExpiration;

    @Transactional
    public UserSession createSession(User user, String deviceId, String deviceName,
                                     String deviceType, String ipAddress, String userAgent) {
        UserSession session = new UserSession();
        session.setSessionToken(UUID.randomUUID().toString());
        session.setUser(user);
        session.setDeviceId(deviceId);
        session.setDeviceName(deviceName);
        session.setDeviceType(deviceType);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(sessionExpiration / 1000));

        return sessionRepository.save(session);
    }

    @Transactional
    public void updateSessionLastAccess(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken).ifPresent(session -> {
            session.setLastAccessedAt(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void terminateSession(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken).ifPresent(session -> {
            session.setActive(false);
            session.setLoggedOutAt(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void terminateAllUserSessions(User user) {
        sessionRepository.deactivateAllUserSessions(user, LocalDateTime.now());
    }

    public List<SessionResponse> getUserActiveSessions(User user) {
        return sessionRepository.findByUserAndActiveTrue(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long countUserActiveSessions(User user) {
        return sessionRepository.countActiveSessionsByUser(user);
    }

    @Transactional
    @Scheduled(cron = "0 */30 * * * ?")  // Run every 30 minutes
    public void cleanupExpiredSessions() {
        log.info("Starting cleanup of expired sessions");
        sessionRepository.deactivateExpiredSessions(LocalDateTime.now());
        log.info("Completed cleanup of expired sessions");
    }

    private SessionResponse mapToResponse(UserSession session) {
        return new SessionResponse(
                session.getId(),
                session.getDeviceId(),
                session.getDeviceName(),
                session.getDeviceType(),
                session.getIpAddress(),
                session.getCreatedAt(),
                session.getLastAccessedAt(),
                session.getActive()
        );
    }
}