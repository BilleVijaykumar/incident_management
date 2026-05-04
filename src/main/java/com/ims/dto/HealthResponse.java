package com.ims.dto;

import java.time.Instant;
import java.util.Map;

/**
 * HealthResponse DTO - Response object for health endpoint
 */
public record HealthResponse(
    String status,
    String application,
    Instant timestamp,
    Map<String, ComponentHealth> components
) {
    public record ComponentHealth(
        String status,
        String details
    ) {}
}