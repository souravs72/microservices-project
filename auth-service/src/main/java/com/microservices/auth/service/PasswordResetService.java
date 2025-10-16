package com.microservices.auth.service;

import com.microservices.auth.entity.PasswordResetToken;
import com.microservices.auth.entity.User;
import com.microservices.auth.repository.PasswordResetTokenRepository;
import com.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.security.password-reset-token-expiration-minutes}")
    private int tokenExpirationMinutes;

    @Transactional
    public void createPasswordResetToken(String email, String ipAddress) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Invalidate all existing tokens for this user
        tokenRepository.invalidateAllUserTokens(user);

        // Create new token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
        resetToken.setRequestIp(ipAddress);

        tokenRepository.save(resetToken);

        // Send email with reset link
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetToken.getToken());

        log.info("Password reset token created for user: {}", user.getUsername());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        if (resetToken.getUsed()) {
            throw new RuntimeException("Password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")  // Run daily at 3 AM
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired password reset tokens");
        tokenRepository.deleteExpiredAndUsedTokens(LocalDateTime.now());
        log.info("Completed cleanup of expired password reset tokens");
    }
}