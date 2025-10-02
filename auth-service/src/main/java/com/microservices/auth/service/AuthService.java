package com.microservices.auth.service;

import com.microservices.auth.dto.*;
import com.microservices.auth.entity.User;
import com.microservices.auth.exception.AccountLockedException;
import com.microservices.auth.exception.AuthenticationException;
import com.microservices.auth.exception.UserAlreadyExistsException;
import com.microservices.auth.repository.UserRepository;
import com.microservices.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.security.max-failed-attempts:3}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration-minutes:30}")
    private long lockoutDurationMinutes;

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        user.setLastLoginIp(ipAddress);
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, "Bearer", user.getUsername(), user.getEmail(), user.getRole());
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // Check if account is locked
        if (user.getAccountLocked()) {
            // Check if lockout period has expired
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

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, "Bearer", user.getUsername(), user.getEmail(), user.getRole());
    }

    public ValidateTokenResponse validateToken(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                return new ValidateTokenResponse(true, username, role);
            }
        } catch (Exception e) {
            // Token is invalid
        }
        return new ValidateTokenResponse(false, null, null);
    }

    private void handleFailedLogin(User user, String ipAddress) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedLoginAt(LocalDateTime.now());

        if (attempts >= maxFailedAttempts) {
            user.setAccountLocked(true);
            user.setAccountLockedAt(LocalDateTime.now());
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
}