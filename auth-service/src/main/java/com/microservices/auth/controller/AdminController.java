package com.microservices.auth.controller;

import com.microservices.auth.dto.SessionResponse;
import com.microservices.auth.entity.User;
import com.microservices.auth.repository.UserRepository;
import com.microservices.auth.service.AdminService;
import com.microservices.auth.service.RefreshTokenService;
import com.microservices.auth.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final SessionService sessionService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @PostMapping("/unlock-account/{username}")
    public ResponseEntity<Map<String, Object>> unlockAccount(@PathVariable String username) {
        boolean unlocked = adminService.unlockUserAccount(username);

        Map<String, Object> response = new HashMap<>();
        if (unlocked) {
            response.put("message", "Account unlocked successfully");
            response.put("username", username);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "User not found or account was not locked");
            response.put("username", username);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/account-status/{username}")
    public ResponseEntity<Map<String, Object>> getAccountStatus(@PathVariable String username) {
        return ResponseEntity.ok(adminService.getAccountSecurityStatus(username));
    }

    @GetMapping("/users/{username}/sessions")
    public ResponseEntity<List<SessionResponse>> getUserSessions(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SessionResponse> sessions = sessionService.getUserActiveSessions(user);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/users/{username}/sessions/terminate-all")
    public ResponseEntity<Map<String, String>> terminateAllUserSessions(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        sessionService.terminateAllUserSessions(user);
        refreshTokenService.revokeAllUserTokens(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All sessions terminated successfully");
        return ResponseEntity.ok(response);
    }
}