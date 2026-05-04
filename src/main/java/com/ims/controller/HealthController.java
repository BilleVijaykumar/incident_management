package com.ims.controller;

import com.ims.dto.HealthResponse;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Controller - Observability endpoints
 * GET /health
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    
    private final Map<String, HealthIndicator> healthIndicators;
    
    public HealthController(java.util.List<HealthIndicator> indicators) {
        this.healthIndicators = new HashMap<>();
        for (HealthIndicator indicator : indicators) {
            healthIndicators.put(indicator.getClass().getSimpleName(), indicator);
        }
    }
    
    /**
     * Health check endpoint
     * GET /health
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        Map<String, HealthResponse.ComponentHealth> components = new HashMap<>();
        
        // Check each component
        boolean allHealthy = true;
        
        for (Map.Entry<String, HealthIndicator> entry : healthIndicators.entrySet()) {
            Health health = entry.getValue().health();
            String status = health.getStatus().getCode();
            String details = health.getDetails() != null ? 
                health.getDetails().toString() : "OK";
            
            components.put(entry.getKey(), new HealthResponse.ComponentHealth(status, details));
            
            if (!"UP".equals(status)) {
                allHealthy = false;
            }
        }
        
        String overallStatus = allHealthy ? "UP" : "DEGRADED";
        
        HealthResponse response = new HealthResponse(
            overallStatus,
            "incident-management-system",
            Instant.now(),
            components
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Simple health check
     * GET /health/simple
     */
    @GetMapping("/health/simple")
    public ResponseEntity<Map<String, Object>> getSimpleHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now());
        health.put("service", "incident-management-system");
        
        return ResponseEntity.ok(health);
    }
}