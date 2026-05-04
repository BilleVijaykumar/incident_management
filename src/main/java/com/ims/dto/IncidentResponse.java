package com.ims.dto;

import java.time.Instant;
import java.util.List;

/**
 * IncidentResponse DTO - Response object for incident queries
 */
public record IncidentResponse(
    Long id,
    String incidentId,
    String title,
    String description,
    String severity,
    String status,
    String componentId,
    String componentType,
    Instant firstSignalTime,
    Instant lastSignalTime,
    Instant createdAt,
    Instant updatedAt,
    Instant resolvedAt,
    Instant closedAt,
    Long signalCount,
    Long mttr,
    RcaResponse rca,
    List<SignalResponse> signals
) {}