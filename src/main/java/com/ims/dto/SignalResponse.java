package com.ims.dto;

import java.time.Instant;

/**
 * SignalResponse DTO - Response object for signal queries
 */
public record SignalResponse(
    String signalId,
    String componentId,
    String componentType,
    String severity,
    String message,
    Instant timestamp,
    String incidentId,
    Instant processedAt
) {}