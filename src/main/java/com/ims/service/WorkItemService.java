package com.ims.service;

import com.ims.cache.DashboardCache;
import com.ims.dto.IncidentResponse;
import com.ims.dto.RcaRequest;
import com.ims.dto.RcaResponse;
import com.ims.dto.SignalResponse;
import com.ims.dto.StatusUpdateRequest;
import com.ims.exception.InvalidStateTransitionException;
import com.ims.exception.ResourceNotFoundException;
import com.ims.exception.RcaValidationException;
import com.ims.model.WorkItem;
import com.ims.model.WorkItem.Status;
import com.ims.model.RCA;
import com.ims.model.Signal;
import com.ims.repository.WorkItemRepository;
import com.ims.repository.RcaRepository;
import com.ims.repository.SignalRepository;
import com.ims.state.IncidentStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * WorkItem Service - Core business logic for work item management
 */
@Service
public class WorkItemService {
    
    private static final Logger log = LoggerFactory.getLogger(WorkItemService.class);
    
    private final WorkItemRepository workItemRepository;
    private final RcaRepository rcaRepository;
    private final SignalRepository signalRepository;
    private final IncidentStateMachine stateMachine;
    private final DashboardCache dashboardCache;
    
    public WorkItemService(
            WorkItemRepository workItemRepository,
            RcaRepository rcaRepository,
            SignalRepository signalRepository,
            IncidentStateMachine stateMachine,
            DashboardCache dashboardCache) {
        this.workItemRepository = workItemRepository;
        this.rcaRepository = rcaRepository;
        this.signalRepository = signalRepository;
        this.stateMachine = stateMachine;
        this.dashboardCache = dashboardCache;
    }
    
    /**
     * Get all active work items
     */
    public List<IncidentResponse> getActiveWorkItems() {
        List<Status> activeStates = List.of(
            Status.OPEN,
            Status.INVESTIGATING,
            Status.RESOLVED
        );
        
        List<WorkItem> workItems = workItemRepository.findActiveWorkItems(activeStates);
        return workItems.stream().map(this::toResponse).toList();
    }
    
    /**
     * Get work item by ID
     */
    public IncidentResponse getWorkItem(Long id) {
        WorkItem workItem = workItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("WorkItem", id));
        return toResponse(workItem);
    }
    
    /**
     * Get work item by work item ID
     */
    public IncidentResponse getWorkItemByWorkItemId(String workItemId) {
        WorkItem workItem = workItemRepository.findByWorkItemId(workItemId)
            .orElseThrow(() -> new ResourceNotFoundException("WorkItem", workItemId));
        return toResponse(workItem);
    }
    
    /**
     * Update work item status
     */
    @Transactional
    public IncidentResponse updateStatus(Long id, StatusUpdateRequest request) {
        WorkItem workItem = workItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("WorkItem", id));
        
        Status currentState = workItem.getStatus();
        Status targetState = Status.valueOf(request.newStatus().toUpperCase());
        
        // Validate state transition
        stateMachine.validateTransition(currentState, targetState);
        
        // Check RCA requirement for CLOSED state
        if (targetState == Status.CLOSED) {
            boolean hasRca = rcaRepository.existsByWorkItemId(id);
            stateMachine.validateCloseRequest(currentState, hasRca);
        }
        
        // Update state
        workItem.setStatus(targetState);
        workItem.setUpdatedAt(Instant.now());
        
        WorkItem saved = workItemRepository.save(workItem);
        dashboardCache.invalidateIncident(workItem.getWorkItemId());
        
        log.info("WorkItem {} transitioned from {} to {}", 
            workItem.getWorkItemId(), currentState, targetState);
        
        return toResponse(saved);
    }
    
    /**
     * Submit RCA for work item
     */
    @Transactional
    public RcaResponse submitRca(Long workItemId, RcaRequest request) {
        WorkItem workItem = workItemRepository.findById(workItemId)
            .orElseThrow(() -> new ResourceNotFoundException("WorkItem", workItemId));
        
        // Validate RCA times
        if (request.incidentEndTime().isBefore(request.incidentStartTime())) {
            throw new RcaValidationException("Incident end time must be after start time");
        }
        
        if (request.incidentEndTime().isAfter(Instant.now())) {
            throw new RcaValidationException("Incident end time cannot be in the future");
        }
        
        // Check if RCA already exists
        if (rcaRepository.existsByWorkItemId(workItemId)) {
            throw new RcaValidationException("RCA already exists for this work item");
        }
        
        // Create RCA
        RCA rca = new RCA();
        rca.setWorkItem(workItem);
        rca.setRootCauseCategory(request.rootCauseCategory());
        rca.setIncidentStartTime(request.incidentStartTime());
        rca.setIncidentEndTime(request.incidentEndTime());
        rca.setFixApplied(request.fixApplied());
        rca.setPreventionSteps(request.preventionSteps());
        rca.setSubmittedAt(Instant.now());
        
        RCA saved = rcaRepository.save(rca);
        
        log.info("RCA submitted for work item {}", workItem.getWorkItemId());
        
        return toRcaResponse(saved);
    }
    
    /**
     * Get RCA for work item
     */
    public RcaResponse getRca(Long workItemId) {
        RCA rca = rcaRepository.findByWorkItemId(workItemId)
            .orElseThrow(() -> new ResourceNotFoundException("RCA for work item", workItemId));
        return toRcaResponse(rca);
    }
    
    /**
     * Get signals for work item
     */
    public List<SignalResponse> getSignalsForWorkItem(String workItemId) {
        List<Signal> signals = signalRepository.findByWorkItemId(workItemId);
        return signals.stream().map(this::toSignalResponse).toList();
    }
    
    /**
     * Convert WorkItem to IncidentResponse
     */
    private IncidentResponse toResponse(WorkItem workItem) {
        RCA rca = rcaRepository.findByWorkItemId(workItem.getId()).orElse(null);
        
        return new IncidentResponse(
            workItem.getId(),
            workItem.getWorkItemId(),
            workItem.getComponentId() + " - Work Item",
            "Work item created from signal aggregation",
            workItem.getSeverity().name(),
            workItem.getStatus().name(),
            workItem.getComponentId(),
            null, // componentType
            workItem.getCreatedAt(),
            workItem.getUpdatedAt(),
            null, // firstSignalTime
            null, // lastSignalTime
            null, // resolvedAt
            null, // closedAt
            workItem.getSignalCount(),
            null, // mttr
            rca != null ? toRcaResponse(rca) : null,
            new ArrayList<>()
        );
    }
    
    /**
     * Convert RCA to RCAResponse
     */
    private RcaResponse toRcaResponse(RCA rca) {
        return new RcaResponse(
            rca.getId(),
            rca.getId().toString(),
            rca.getWorkItem().getId(),
            rca.getRootCauseCategory(),
            rca.getIncidentStartTime(),
            rca.getIncidentEndTime(),
            rca.getFixApplied(),
            rca.getPreventionSteps(),
            null, // additionalNotes
            "system", // submittedBy
            rca.getSubmittedAt()
        );
    }
    
    /**
     * Convert Signal to SignalResponse
     */
    private SignalResponse toSignalResponse(Signal signal) {
        return new SignalResponse(
            signal.getId(),
            signal.getComponentId(),
            signal.getComponentType(),
            signal.getSeverity(),
            signal.getMessage(),
            signal.getTimestamp(),
            signal.getWorkItemId(),
            signal.getProcessedAt()
        );
    }
}