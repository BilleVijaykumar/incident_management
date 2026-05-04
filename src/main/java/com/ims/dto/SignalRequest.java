package com.ims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Signal DTO - Request object for ingesting signals
 */
public record SignalRequest(
    @NotBlank(message = "Component ID is required")
    String componentId,
    
    @NotBlank(message = "Component type is required")
    String componentType,
    
    @NotBlank(message = "Severity is required")
    String severity,
    
    @NotBlank(message = "Message is required")
    String message,
    
    @NotNull(message = "Timestamp is required")
    Instant timestamp
) {
    // Compact constructor for validation normalization
    public SignalRequest {
        if (componentId != null) componentId = componentId.trim();
        if (componentType != null) componentType = componentType.trim().toUpperCase();
        if (severity != null) severity = severity.trim().toUpperCase();
        if (message != null) message = message.trim();
    }
}