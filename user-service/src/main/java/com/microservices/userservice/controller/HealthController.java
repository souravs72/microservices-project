package com.microservices.userservice.controller;

import com.microservices.userservice.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final ReconciliationService reconciliationService;

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = Map.of(
                "status", "UP",
                "service", "user-service",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reconciliation")
    public ResponseEntity<Map<String, Object>> getReconciliationMetrics() {
        Map<String, Object> metrics = reconciliationService.getReconciliationMetrics();
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/reconciliation/force")
    public ResponseEntity<Map<String, Object>> forceReconciliation() {
        Map<String, Object> result = reconciliationService.forceFullReconciliation();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reconciliation/user/{userId}")
    public ResponseEntity<Map<String, Object>> reconcileUser(@PathVariable Long userId) {
        try {
            var userDTO = reconciliationService.reconcileUserById(userId);
            Map<String, Object> result = Map.of(
                    "success", true,
                    "message", "User reconciled successfully",
                    "user", userDTO
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = Map.of(
                    "success", false,
                    "message", "Failed to reconcile user: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(result);
        }
    }
}






