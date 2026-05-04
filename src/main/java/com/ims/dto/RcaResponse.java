package com.ims.dto;

import java.time.Instant;

/**
 * RCAResponse DTO - Response object for RCA queries
 */
public record RcaResponse(
    Long id,
    String rcaId,
    Long incidentId,
    String rootCauseCategory,
    Instant incidentStartTime,
    Instant incidentEndTime,
    String fixApplied,
    String preventionSteps,
    String additionalNotes,
    String submittedBy,
    Instant submittedAt
) {}