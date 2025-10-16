package com.microservices.auth.service;

import com.microservices.auth.entity.RefreshToken;
import com.microservices.auth.entity.User;
import com.microservices.auth.repository.RefreshTokenRepository;
import com.microservices.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceId, String deviceName,
                                           String ipAddress, String userAgent) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        refreshToken.setDeviceId(deviceId);
        refreshToken.setDeviceName(deviceName);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again");
        }

        if (token.getRevoked()) {
            throw new RuntimeException("Refresh token has been revoked. Please login again");
        }

        return token;
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
    }

    @Transactional
    public void revokeUserDeviceTokens(User user, String deviceId) {
        refreshTokenRepository.revokeUserDeviceTokens(user, deviceId, LocalDateTime.now());
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")  // Run daily at 2 AM
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Completed cleanup of expired refresh tokens");
    }

    public List<RefreshToken> getUserTokens(User user) {
        return refreshTokenRepository.findByUser(user);
    }
}