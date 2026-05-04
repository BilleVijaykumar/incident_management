package com.ims.dto;

import java.time.Instant;

/**
 * StatusUpdateRequest DTO - Request object for incident status transitions
 */
public record StatusUpdateRequest(
    String newStatus,
    String reason,
    String updatedBy
) {}