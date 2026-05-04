package com.ims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * RCARequest DTO - Request object for Root Cause Analysis
 */
public record RcaRequest(
    @NotBlank(message = "Root cause category is required")
    String rootCauseCategory,
    
    @NotNull(message = "Incident start time is required")
    Instant incidentStartTime,
    
    @NotNull(message = "Incident end time is required")
    Instant incidentEndTime,
    
    @NotBlank(message = "Fix applied is required")
    String fixApplied,
    
    @NotBlank(message = "Prevention steps are required")
    String preventionSteps,
    
    String additionalNotes
) {}