package com.microservices.auth.service;

import com.microservices.auth.entity.IpLockout;
import com.microservices.auth.entity.LoginAttempt;
import com.microservices.auth.repository.IpLockoutRepository;
import com.microservices.auth.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for handling IP-based login protection and rate limiting.
 * Implements security measures to prevent brute force attacks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IpLoginProtectionService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final IpLockoutRepository ipLockoutRepository;

    // Configuration constants
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final int ATTEMPT_WINDOW_MINUTES = 5;

    /**
     * Check if IP address is currently locked
     */
    public boolean isIpLocked(String ipAddress) {
        Optional<IpLockout> lockout = ipLockoutRepository.findByIpAddress(ipAddress);
        return lockout.map(IpLockout::isLocked).orElse(false);
    }

    /**
     * Record a successful login attempt
     */
    public void recordSuccessfulLogin(String ipAddress, String username, String userAgent) {
        log.info("Recording successful login for IP: {} username: {}", ipAddress, username);
        
        // Record the successful attempt
        LoginAttempt attempt = new LoginAttempt(ipAddress, username, userAgent);
        loginAttemptRepository.save(attempt);
        
        // Reset any existing lockout for this IP
        Optional<IpLockout> lockout = ipLockoutRepository.findByIpAddress(ipAddress);
        if (lockout.isPresent()) {
            IpLockout ipLockout = lockout.get();
            ipLockout.resetFailedAttempts();
            ipLockoutRepository.save(ipLockout);
            log.info("Reset failed attempts for IP: {}", ipAddress);
        }
        
        // Clean up expired attempts
        cleanupExpiredAttempts();
    }

    /**
     * Record a failed login attempt
     */
    public void recordFailedLogin(String ipAddress, String username, String failureReason, String userAgent) {
        log.warn("Recording failed login for IP: {} username: {} reason: {}", ipAddress, username, failureReason);
        
        // Record the failed attempt
        LoginAttempt attempt = new LoginAttempt(ipAddress, username, failureReason, userAgent);
        loginAttemptRepository.save(attempt);
        
        // Get or create IP lockout record
        IpLockout lockout = ipLockoutRepository.findByIpAddress(ipAddress)
                .orElse(new IpLockout(ipAddress));
        
        // Increment failed attempts
        lockout.incrementFailedAttempts();
        ipLockoutRepository.save(lockout);
        
        log.warn("IP {} now has {} failed attempts", ipAddress, lockout.getFailedAttempts());
        
        // Check if IP should be locked
        if (lockout.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            log.warn("Locking IP {} for {} minutes due to {} failed attempts", 
                    ipAddress, LOCKOUT_DURATION_MINUTES, lockout.getFailedAttempts());
        }
        
        // Clean up expired attempts
        cleanupExpiredAttempts();
    }

    /**
     * Check if login should be allowed for the given IP
     */
    public boolean isLoginAllowed(String ipAddress) {
        // Check if IP is currently locked
        if (isIpLocked(ipAddress)) {
            log.warn("Login blocked for locked IP: {}", ipAddress);
            return false;
        }
        
        // Check recent failed attempts
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(ATTEMPT_WINDOW_MINUTES);
        long recentFailedAttempts = loginAttemptRepository.countFailedAttemptsByIpSince(ipAddress, windowStart);
        
        if (recentFailedAttempts >= MAX_FAILED_ATTEMPTS) {
            log.warn("Login blocked for IP {} due to {} recent failed attempts", ipAddress, recentFailedAttempts);
            return false;
        }
        
        return true;
    }

    /**
     * Get remaining lockout time for an IP address
     */
    public long getRemainingLockoutMinutes(String ipAddress) {
        Optional<IpLockout> lockout = ipLockoutRepository.findByIpAddress(ipAddress);
        return lockout.map(IpLockout::getRemainingLockoutMinutes).orElse(0L);
    }

    /**
     * Get failed attempts count for an IP address
     */
    public int getFailedAttemptsCount(String ipAddress) {
        Optional<IpLockout> lockout = ipLockoutRepository.findByIpAddress(ipAddress);
        return lockout.map(IpLockout::getFailedAttempts).orElse(0);
    }

    /**
     * Manually unlock an IP address (admin function)
     */
    public void unlockIp(String ipAddress) {
        Optional<IpLockout> lockout = ipLockoutRepository.findByIpAddress(ipAddress);
        if (lockout.isPresent()) {
            IpLockout ipLockout = lockout.get();
            ipLockout.resetFailedAttempts();
            ipLockoutRepository.save(ipLockout);
            log.info("Manually unlocked IP: {}", ipAddress);
        }
    }

    /**
     * Get login attempt statistics for an IP address
     */
    public LoginAttemptStats getLoginAttemptStats(String ipAddress) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(ATTEMPT_WINDOW_MINUTES);
        long recentFailedAttempts = loginAttemptRepository.countFailedAttemptsByIpSince(ipAddress, windowStart);
        long totalAttempts = loginAttemptRepository.countByIpAddress(ipAddress);
        
        return new LoginAttemptStats(
                ipAddress,
                totalAttempts,
                recentFailedAttempts,
                isIpLocked(ipAddress),
                getRemainingLockoutMinutes(ipAddress)
        );
    }

    /**
     * Clean up expired login attempts and lockouts
     */
    @Transactional
    public void cleanupExpiredAttempts() {
        LocalDateTime now = LocalDateTime.now();
        
        // Delete expired login attempts
        int deletedAttempts = loginAttemptRepository.deleteExpiredAttempts(now);
        if (deletedAttempts > 0) {
            log.debug("Cleaned up {} expired login attempts", deletedAttempts);
        }
        
        // Delete expired lockouts
        int deletedLockouts = ipLockoutRepository.deleteExpiredLockouts(now);
        if (deletedLockouts > 0) {
            log.debug("Cleaned up {} expired IP lockouts", deletedLockouts);
        }
    }

    /**
     * Data class for login attempt statistics
     */
    public static class LoginAttemptStats {
        private final String ipAddress;
        private final long totalAttempts;
        private final long recentFailedAttempts;
        private final boolean isLocked;
        private final long remainingLockoutMinutes;

        public LoginAttemptStats(String ipAddress, long totalAttempts, long recentFailedAttempts, 
                               boolean isLocked, long remainingLockoutMinutes) {
            this.ipAddress = ipAddress;
            this.totalAttempts = totalAttempts;
            this.recentFailedAttempts = recentFailedAttempts;
            this.isLocked = isLocked;
            this.remainingLockoutMinutes = remainingLockoutMinutes;
        }

        // Getters
        public String getIpAddress() { return ipAddress; }
        public long getTotalAttempts() { return totalAttempts; }
        public long getRecentFailedAttempts() { return recentFailedAttempts; }
        public boolean isLocked() { return isLocked; }
        public long getRemainingLockoutMinutes() { return remainingLockoutMinutes; }
    }
}
