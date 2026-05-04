package com.ims.service;

import com.ims.cache.DashboardCache;
import com.ims.dto.IncidentResponse;
import com.ims.dto.RcaRequest;
import com.ims.dto.RcaResponse;
import com.ims.dto.StatusUpdateRequest;
import com.ims.exception.InvalidStateTransitionException;
import com.ims.exception.ResourceNotFoundException;
import com.ims.exception.RcaValidationException;
import com.ims.model.WorkItem;
import com.ims.model.WorkItem.Status;
import com.ims.model.WorkItem.Severity;
import com.ims.model.RCA;
import com.ims.repository.WorkItemRepository;
import com.ims.repository.RcaRepository;
import com.ims.repository.SignalRepository;
import com.ims.state.IncidentStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkItemServiceTest {

    @Mock
    private WorkItemRepository workItemRepository;
    
    @Mock
    private RcaRepository rcaRepository;
    
    @Mock
    private SignalRepository signalRepository;
    
    @Mock
    private IncidentStateMachine stateMachine;
    
    @Mock
    private DashboardCache dashboardCache;
    
    private WorkItemService workItemService;

    @BeforeEach
    void setUp() {
        workItemService = new WorkItemService(
            workItemRepository,
            rcaRepository,
            signalRepository,
            stateMachine,
            dashboardCache
        );
    }

    @Test
    void getWorkItem_Found() {
        // Arrange
        WorkItem workItem = createTestWorkItem();
        when(workItemRepository.findById(1L)).thenReturn(Optional.of(workItem));
        when(rcaRepository.findByWorkItemId(1L)).thenReturn(Optional.empty());
        
        // Act
        IncidentResponse response = workItemService.getWorkItem(1L);
        
        // Assert
        assertNotNull(response);
        assertEquals("WI-TEST123", response.incidentId());
    }

    @Test
    void getWorkItem_NotFound() {
        // Arrange
        when(workItemRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            workItemService.getWorkItem(1L);
        });
    }

    @Test
    void updateStatus_Success() {
        // Arrange
        WorkItem workItem = createTestWorkItem();
        StatusUpdateRequest request = new StatusUpdateRequest("INVESTIGATING", "Team assigned", "user@test.com");
        
        when(workItemRepository.findById(1L)).thenReturn(Optional.of(workItem));
        doNothing().when(stateMachine).validateTransition(any(), any());
        when(workItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // Act
        IncidentResponse response = workItemService.updateStatus(1L, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(Status.INVESTIGATING, response.status());
    }

    @Test
    void updateStatus_InvalidTransition() {
        // Arrange
        WorkItem workItem = createTestWorkItem();
        StatusUpdateRequest request = new StatusUpdateRequest("CLOSED", "Done", "user@test.com");
        
        when(workItemRepository.findById(1L)).thenReturn(Optional.of(workItem));
        doThrow(new InvalidStateTransitionException("OPEN", "CLOSED"))
            .when(stateMachine).validateTransition(any(), any());
        
        // Act & Assert
        assertThrows(InvalidStateTransitionException.class, () -> {
            workItemService.updateStatus(1L, request);
        });
    }

    @Test
    void submitRca_Success() {
        // Arrange
        WorkItem workItem = createTestWorkItem();
        
        RcaRequest request = new RcaRequest(
            "HARDWARE",
            Instant.now().minusSeconds(3600),
            Instant.now(),
            "Replaced failed disk",
            "Added monitoring",
            null
        );
        
        when(workItemRepository.findById(1L)).thenReturn(Optional.of(workItem));
        when(rcaRepository.existsByWorkItemId(1L)).thenReturn(false);
        when(rcaRepository.save(any())).thenAnswer(i -> {
            RCA rca = i.getArgument(0);
            rca.setId(1L);
            return rca;
        });
        when(workItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // Act
        RcaResponse response = workItemService.submitRca(1L, request);
        
        // Assert
        assertNotNull(response);
        assertEquals("HARDWARE", response.rootCauseCategory());
    }

    @Test
    void submitRca_AlreadyExists() {
        // Arrange
        WorkItem workItem = createTestWorkItem();
        RcaRequest request = new RcaRequest(
            "HARDWARE",
            Instant.now().minusSeconds(3600),
            Instant.now(),
            "Replaced failed disk",
            "Added monitoring",
            null
        );
        
        when(workItemRepository.findById(1L)).thenReturn(Optional.of(workItem));
        when(rcaRepository.existsByWorkItemId(1L)).thenReturn(true);
        
        // Act & Assert
        assertThrows(RcaValidationException.class, () -> {
            workItemService.submitRca(1L, request);
        });
    }

    @Test
    void submitRca_EndTimeBeforeStartTime() {
        // Arrange
        WorkItem workItem = createTestWorkItem();
        RcaRequest request = new RcaRequest(
            "HARDWARE",
            Instant.now(),
            Instant.now().minusSeconds(3600), // End before start
            "Replaced failed disk",
            "Added monitoring",
            null
        );
        
        when(workItemRepository.findById(1L)).thenReturn(Optional.of(workItem));
        
        // Act & Assert
        assertThrows(RcaValidationException.class, () -> {
            workItemService.submitRca(1L, request);
        });
    }

    private WorkItem createTestWorkItem() {
        WorkItem workItem = new WorkItem();
        workItem.setId(1L);
        workItem.setWorkItemId("WI-TEST123");
        workItem.setComponentId("CACHE_CLUSTER_01");
        workItem.setSeverity(Severity.P2);
        workItem.setStatus(Status.OPEN);
        workItem.setCreatedAt(Instant.now());
        workItem.setUpdatedAt(Instant.now());
        workItem.setSignalCount(100L);
        return workItem;
    }
}