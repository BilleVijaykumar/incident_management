package com.ims.service;

import com.ims.cache.DebounceManager;
import com.ims.cache.RateLimiter;
import com.ims.config.KafkaConfig;
import com.ims.dto.SignalRequest;
import com.ims.exception.RateLimitExceededException;
import com.ims.model.WorkItem;
import com.ims.repository.WorkItemRepository;
import com.ims.repository.SignalRepository;
import com.ims.strategy.AlertStrategyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest {

    @Mock
    private SignalRepository signalRepository;
    
    @Mock
    private WorkItemRepository workItemRepository;
    
    @Mock
    private DebounceManager debounceManager;
    
    @Mock
    private RateLimiter rateLimiter;
    
    @Mock
    private AlertStrategyFactory alertStrategyFactory;
    
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private ObjectMapper objectMapper;
    private SignalService signalService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        signalService = new SignalService(
            signalRepository,
            workItemRepository,
            debounceManager,
            rateLimiter,
            alertStrategyFactory,
            kafkaTemplate,
            objectMapper
        );
    }

    @Test
    void ingestSignal_Success() {
        // Arrange
        SignalRequest request = new SignalRequest(
            "CACHE_CLUSTER_01",
            "CACHE",
            "P2",
            "Redis latency spike detected",
            Instant.now()
        );
        
        when(rateLimiter.tryAcquireWithBurst(anyString(), anyInt(), anyInt(), any()))
            .thenReturn(true);
        
        when(alertStrategyFactory.determineSeverity(any())).thenReturn("P2");
        
        // Act
        String signalId = signalService.ingestSignal(request);
        
        // Assert
        assertNotNull(signalId);
        assertTrue(signalId.startsWith("SIG-"));
        verify(kafkaTemplate).send(eq(KafkaConfig.SIGNAL_TOPIC), anyString(), anyString());
    }

    @Test
    void ingestSignal_RateLimitExceeded() {
        // Arrange
        SignalRequest request = new SignalRequest(
            "CACHE_CLUSTER_01",
            "CACHE",
            "P2",
            "Redis latency spike detected",
            Instant.now()
        );
        
        when(rateLimiter.tryAcquireWithBurst(anyString(), anyInt(), anyInt(), any()))
            .thenReturn(false);
        
        // Act & Assert
        assertThrows(RateLimitExceededException.class, () -> {
            signalService.ingestSignal(request);
        });
        
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void processSignalEvent_NewIncident() {
        // Arrange
        SignalRequest request = new SignalRequest(
            "CACHE_CLUSTER_01",
            "CACHE",
            "P2",
            "Redis latency spike detected",
            Instant.now()
        );
        
        SignalService.SignalEvent event = new SignalService.SignalEvent(
            "SIG-TEST1234",
            request,
            "P2"
        );
        
        when(debounceManager.checkAndCreateWorkItem(anyString(), anyString()))
            .thenReturn("WI-NEW123"); // Threshold reached - create new work item
        
        when(signalRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(workItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // Act
        signalService.processSignalEvent(event);
        
        // Assert
        verify(signalRepository).save(any());
        verify(workItemRepository).save(any());
    }

    @Test
    void processSignalEvent_DebouncedIncident() {
        // Arrange
        SignalRequest request = new SignalRequest(
            "CACHE_CLUSTER_01",
            "CACHE",
            "P2",
            "Redis latency spike detected",
            Instant.now()
        );
        
        SignalService.SignalEvent event = new SignalService.SignalEvent(
            "SIG-TEST1234",
            request,
            "P2"
        );
        
        when(debounceManager.checkAndCreateWorkItem(anyString(), anyString()))
            .thenReturn(null); // Still accumulating
        
        when(signalRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // Act
        signalService.processSignalEvent(event);
        
        // Assert
        verify(signalRepository).save(any());
        verify(workItemRepository, never()).save(any()); // No new incident
    }
}