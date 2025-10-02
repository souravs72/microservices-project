package com.microservices.auth.controller;

import com.microservices.auth.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin controller for administrative operations
 * In production, secure this with proper admin authentication
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Manually unlock a user account
     * IMPORTANT: In production, secure this endpoint with admin role check
     */
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

    /**
     * Get account security status
     */
    @GetMapping("/account-status/{username}")
    public ResponseEntity<Map<String, Object>> getAccountStatus(@PathVariable String username) {
        return ResponseEntity.ok(adminService.getAccountSecurityStatus(username));
    }
}