package com.microservices.auth.controller;

import com.microservices.auth.dto.*;
import com.microservices.auth.service.AuthService;
import com.microservices.auth.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
            @RequestHeader(value = "X-Device-Name", required = false) String deviceName,
            @RequestHeader(value = "X-Device-Type", required = false, defaultValue = "WEB") String deviceType,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        DeviceInfo deviceInfo = new DeviceInfo(deviceId, deviceName, deviceType, userAgent);

        AuthResponse response = authService.register(request, ipAddress, deviceInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
            @RequestHeader(value = "X-Device-Name", required = false) String deviceName,
            @RequestHeader(value = "X-Device-Type", required = false, defaultValue = "WEB") String deviceType,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        DeviceInfo deviceInfo = new DeviceInfo(deviceId, deviceName, deviceType, userAgent);

        AuthResponse response = authService.login(request, ipAddress, deviceInfo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateTokenResponse> validateToken(
            @Valid @RequestBody ValidateTokenRequest request) {
        ValidateTokenResponse response = authService.validateToken(request.getToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        passwordResetService.createPasswordResetToken(request.getEmail(), ipAddress);

        Map<String, String> response = new HashMap<>();
        response.put("message", "If the email exists, a password reset link has been sent");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Auth Service");
        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}