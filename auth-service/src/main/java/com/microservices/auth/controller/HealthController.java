package com.microservices.auth.controller;

import com.microservices.auth.entity.OutboxEvent;
import com.microservices.auth.repository.OutboxEventRepository;
import com.microservices.auth.service.HealthCheckService;
import com.microservices.auth.service.OutboxEventProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final OutboxEventRepository outboxEventRepository;
    private final HealthCheckService healthCheckService;
    private final OutboxEventProcessor outboxEventProcessor;

    @GetMapping("/overall")
    public ResponseEntity<Map<String, Object>> getOverallHealth() {
        Map<String, Object> health = healthCheckService.getHealthStatus();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/sync-status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        Map<String, Object> status = healthCheckService.getSynchronizationStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/outbox")
    public ResponseEntity<Map<String, Object>> getOutboxHealth() {
        Map<String, Object> outboxHealth = outboxEventProcessor.getOutboxHealth();
        return ResponseEntity.ok(outboxHealth);
    }

    @GetMapping("/outbox-events")
    public ResponseEntity<Map<String, Object>> getOutboxEvents() {
        Map<String, Object> events = new HashMap<>();
        
        // Get recent unprocessed events
        var unprocessedEvents = outboxEventRepository.findUnprocessedEvents(
            org.springframework.data.domain.PageRequest.of(0, 10)
        );
        
        events.put("unprocessedEvents", unprocessedEvents);
        events.put("count", unprocessedEvents.size());
        
        return ResponseEntity.ok(events);
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
