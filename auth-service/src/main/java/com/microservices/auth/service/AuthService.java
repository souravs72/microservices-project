package com.microservices.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.auth.dto.*;
import com.microservices.auth.entity.OutboxEvent;
import com.microservices.auth.entity.RefreshToken;
import com.microservices.auth.entity.User;
import com.microservices.auth.entity.UserSession;
import com.microservices.auth.exception.AccountLockedException;
import com.microservices.auth.exception.AuthenticationException;
import com.microservices.auth.exception.UserAlreadyExistsException;
import com.microservices.auth.repository.OutboxEventRepository;
import com.microservices.auth.repository.UserRepository;
import com.microservices.auth.util.InputSanitizer;
import com.microservices.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;
    private final EmailService emailService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration-minutes:30}")
    private long lockoutDurationMinutes;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress, DeviceInfo deviceInfo) {
        // Sanitize inputs
        String sanitizedUsername = InputSanitizer.sanitizeUsername(request.getUsername());
        String sanitizedEmail = InputSanitizer.sanitizeEmail(request.getEmail());

        if (userRepository.existsByUsername(sanitizedUsername)) {
            throw new UserAlreadyExistsException("User already exists");
        }
        if (userRepository.existsByEmail(sanitizedEmail)) {
            throw new UserAlreadyExistsException("User already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(sanitizedUsername);
        user.setEmail(sanitizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        user.setLastLoginIp(ipAddress);
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);

        user = userRepository.save(user);

        // Create session
        UserSession session = sessionService.createSession(
                user,
                deviceInfo.getDeviceId(),
                deviceInfo.getDeviceName(),
                deviceInfo.getDeviceType(),
                ipAddress,
                deviceInfo.getUserAgent()
        );

        // Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user,
                deviceInfo.getDeviceId(),
                deviceInfo.getDeviceName(),
                ipAddress,
                deviceInfo.getUserAgent()
        );

        // Publish event to outbox (will be processed by scheduler)
        publishUserCreatedEvent(user, request.getFirstName(), request.getLastName());

        // Generate access token
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());

        log.info("User registered successfully: {} from IP: {}", sanitizedUsername, ipAddress);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, DeviceInfo deviceInfo) {
        String sanitizedUsername = InputSanitizer.sanitizeUsername(request.getUsername());

        User user = userRepository.findByUsername(sanitizedUsername)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // Check if account is locked
        if (user.getAccountLocked()) {
            if (isLockoutExpired(user)) {
                unlockAccount(user);
            } else {
                throw new AccountLockedException(
                        "Account is locked due to multiple failed login attempts. " +
                                "Please try again later or contact support."
                );
            }
        }

        // Check if account is enabled
        if (!user.getEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user, ipAddress);
            throw new AuthenticationException("Invalid credentials");
        }

        // Successful login - reset failed attempts and update login info
        handleSuccessfulLogin(user, ipAddress);

        // Create session
        UserSession session = sessionService.createSession(
                user,
                deviceInfo.getDeviceId(),
                deviceInfo.getDeviceName(),
                deviceInfo.getDeviceType(),
                ipAddress,
                deviceInfo.getUserAgent()
        );

        // Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user,
                deviceInfo.getDeviceId(),
                deviceInfo.getDeviceName(),
                ipAddress,
                deviceInfo.getUserAgent()
        );

        // Generate access token
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());

        log.info("User logged in successfully: {} from IP: {}", sanitizedUsername, ipAddress);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());

        // Optionally rotate refresh token for better security
        String newRefreshToken = refreshToken.getToken();
        // Uncomment below to enable refresh token rotation
        // refreshTokenService.revokeToken(refreshToken.getToken());
        // RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken(
        //         user,
        //         refreshToken.getDeviceId(),
        //         refreshToken.getDeviceName(),
        //         refreshToken.getIpAddress(),
        //         refreshToken.getUserAgent()
        // );
        // newRefreshToken = newRefreshTokenEntity.getToken();

        log.info("Access token refreshed for user: {}", user.getUsername());

        return new RefreshTokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                accessTokenExpiration / 1000  // Convert to seconds
        );
    }

    public ValidateTokenResponse validateToken(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                return new ValidateTokenResponse(true, username, role);
            }
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
        }
        return new ValidateTokenResponse(false, null, null);
    }

    @Transactional
    public void logout(String refreshToken) {
        try {
            refreshTokenService.revokeToken(refreshToken);
            log.info("User logged out successfully");
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
        }
    }

    private void handleFailedLogin(User user, String ipAddress) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedLoginAt(LocalDateTime.now());

        if (attempts >= maxFailedAttempts) {
            user.setAccountLocked(true);
            user.setAccountLockedAt(LocalDateTime.now());

            // Send email notification
            emailService.sendAccountLockedEmail(user.getEmail(), user.getUsername());

            log.warn("Account locked for user: {} after {} failed attempts from IP: {}",
                    user.getUsername(), attempts, ipAddress);
        }

        userRepository.save(user);
    }

    private void handleSuccessfulLogin(User user, String ipAddress) {
        user.setFailedLoginAttempts(0);
        user.setLastFailedLoginAt(null);
        user.setAccountLocked(false);
        user.setAccountLockedAt(null);
        user.setLastLoginIp(ipAddress);
        user.setLastLoginAt(LocalDateTime.now());

        userRepository.save(user);
    }

    private boolean isLockoutExpired(User user) {
        if (user.getAccountLockedAt() == null) {
            return true;
        }

        LocalDateTime unlockTime = user.getAccountLockedAt().plusMinutes(lockoutDurationMinutes);
        return LocalDateTime.now().isAfter(unlockTime);
    }

    private void unlockAccount(User user) {
        user.setAccountLocked(false);
        user.setAccountLockedAt(null);
        user.setFailedLoginAttempts(0);
        user.setLastFailedLoginAt(null);
        userRepository.save(user);
    }

    private void publishUserCreatedEvent(User user, String firstName, String lastName) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("username", user.getUsername());
            payload.put("email", user.getEmail());
            payload.put("firstName", firstName);
            payload.put("lastName", lastName);
            payload.put("eventType", "USER_CREATED");
            payload.put("timestamp", System.currentTimeMillis());

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateType("USER");
            outboxEvent.setAggregateId(user.getId().toString());
            outboxEvent.setEventType("USER_CREATED");
            outboxEvent.setPayload(objectMapper.writeValueAsString(payload));
            outboxEvent.setCorrelationId(MDC.get("correlationId"));

            outboxEventRepository.save(outboxEvent);

            log.info("User created event published to outbox for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to publish user created event to outbox: {}", e.getMessage(), e);
        }
    }
}