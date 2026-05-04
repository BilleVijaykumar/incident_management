package com.ims.controller;

import com.ims.dto.*;
import com.ims.service.WorkItemService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * WorkItem Controller - REST API for work item management
 * GET /incidents, PATCH /incidents/{id}/status, POST /incidents/{id}/rca
 */
@RestController
@RequestMapping("/api/incidents")
public class WorkItemController {
    
    private static final Logger log = LoggerFactory.getLogger(WorkItemController.class);
    
    private final WorkItemService workItemService;
    
    public WorkItemController(WorkItemService workItemService) {
        this.workItemService = workItemService;
    }
    
    /**
     * Get all active work items
     * GET /incidents/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getActiveWorkItems() {
        List<IncidentResponse> workItems = workItemService.getActiveWorkItems();
        return ResponseEntity.ok(ApiResponse.success(workItems));
    }
    
    /**
     * Get work item by ID
     * GET /incidents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getWorkItem(@PathVariable Long id) {
        IncidentResponse workItem = workItemService.getWorkItem(id);
        return ResponseEntity.ok(ApiResponse.success(workItem));
    }
    
    /**
     * Get work item by work item ID
     * GET /incidents/by-work-item-id/{workItemId}
     */
    @GetMapping("/by-work-item-id/{workItemId}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getWorkItemByWorkItemId(
            @PathVariable String workItemId) {
        IncidentResponse workItem = workItemService.getWorkItemByWorkItemId(workItemId);
        return ResponseEntity.ok(ApiResponse.success(workItem));
    }
    
    /**
     * Update work item status
     * PATCH /incidents/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        
        log.info("Updating work item {} status to {}", id, request.newStatus());
        
        IncidentResponse workItem = workItemService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(workItem, "Status updated"));
    }
    
    /**
     * Submit RCA for work item
     * POST /incidents/{id}/rca
     */
    @PostMapping("/{id}/rca")
    public ResponseEntity<ApiResponse<RcaResponse>> submitRca(
            @PathVariable Long id,
            @Valid @RequestBody RcaRequest request) {
        
        log.info("Submitting RCA for work item {}", id);
        
        RcaResponse rca = workItemService.submitRca(id, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(rca, "RCA submitted successfully"));
    }
    
    /**
     * Get RCA for work item
     * GET /incidents/{id}/rca
     */
    @GetMapping("/{id}/rca")
    public ResponseEntity<ApiResponse<RcaResponse>> getRca(@PathVariable Long id) {
        RcaResponse rca = workItemService.getRca(id);
        return ResponseEntity.ok(ApiResponse.success(rca));
    }
    
    /**
     * Get signals for work item
     * GET /incidents/{id}/signals
     */
    @GetMapping("/{id}/signals")
    public ResponseEntity<ApiResponse<List<SignalResponse>>> getWorkItemSignals(
            @PathVariable Long id) {
        
        IncidentResponse workItem = workItemService.getWorkItem(id);
        List<SignalResponse> signals = workItemService.getSignalsForWorkItem(workItem.incidentId());
        return ResponseEntity.ok(ApiResponse.success(signals));
    }
}