package com.ims.service;

import com.ims.cache.DebounceManager;
import com.ims.cache.RateLimiter;
import com.ims.config.KafkaConfig;
import com.ims.dto.SignalRequest;
import com.ims.exception.RateLimitExceededException;
import com.ims.model.WorkItem;
import com.ims.model.WorkItem.Severity;
import com.ims.model.Signal;
import com.ims.repository.WorkItemRepository;
import com.ims.repository.SignalRepository;
import com.ims.strategy.AlertStrategyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Signal Service - Handles signal ingestion with rate limiting and debouncing
 */
@Service
public class SignalService {
    
    private static final Logger log = LoggerFactory.getLogger(SignalService.class);
    
    private final SignalRepository signalRepository;
    private final WorkItemRepository workItemRepository;
    private final DebounceManager debounceManager;
    private final RateLimiter rateLimiter;
    private final AlertStrategyFactory alertStrategyFactory;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ims.rate-limit.requests-per-second:1000}")
    private int rateLimitPerSecond;
    
    @Value("${ims.rate-limit.burst-capacity:2000}")
    private int burstCapacity;
    
    public SignalService(
            SignalRepository signalRepository,
            WorkItemRepository workItemRepository,
            DebounceManager debounceManager,
            RateLimiter rateLimiter,
            AlertStrategyFactory alertStrategyFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.signalRepository = signalRepository;
        this.workItemRepository = workItemRepository;
        this.debounceManager = debounceManager;
        this.rateLimiter = rateLimiter;
        this.alertStrategyFactory = alertStrategyFactory;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Ingest signal - async, non-blocking with rate limiting
     */
    public String ingestSignal(SignalRequest request) {
        // 1. Rate limiting check
        if (!rateLimiter.tryAcquireWithBurst("signals", rateLimitPerSecond, burstCapacity, Duration.ofSeconds(1))) {
            throw new RateLimitExceededException();
        }
        
        // 2. Determine severity using strategy pattern
        String severity = alertStrategyFactory.determineSeverity(request);
        
        // 3. Generate signal ID
        String signalId = "SIG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        try {
            // 4. Send to Kafka for async processing (backpressure handling)
            SignalEvent event = new SignalEvent(signalId, request, severity);
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.SIGNAL_TOPIC, signalId, eventJson);
            
            log.info("Signal {} queued for processing", signalId);
            return signalId;
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize signal event", e);
            throw new RuntimeException("Failed to process signal", e);
        }
    }
    
    /**
     * Process signal from Kafka - called by worker
     */
    public void processSignalEvent(SignalEvent event) {
        SignalRequest request = event.signalRequest();
        String severity = event.severity();
        
        // 1. Save raw signal to MongoDB
        Signal signal = new Signal();
        signal.setId(event.signalId());
        signal.setComponentId(request.componentId());
        signal.setComponentType(request.componentType());
        signal.setSeverity(severity);
        signal.setMessage(request.message());
        signal.setTimestamp(request.timestamp());
        signal.setProcessedAt(Instant.now());
        
        // 2. Check if we should create a work item
        String workItemId = debounceManager.checkAndCreateWorkItem(request.componentId(), severity);
        
        if (workItemId != null) {
            // Threshold reached - create work item and link all accumulated signals
            signal.setWorkItemId(workItemId);
            createWorkItem(workItemId, request.componentId(), severity);
            
            // Link all previous signals for this component to the work item
            linkSignalsToWorkItem(request.componentId(), workItemId);
            
            log.info("Work item {} created for component {} with {} signals", 
                workItemId, request.componentId(), debounceManager.getSignalCount(request.componentId()) + 1);
        } else {
            // Still accumulating signals
            signal.setWorkItemId(null); // Will be linked later when work item is created
            log.debug("Signal {} accumulated for component {}", event.signalId(), request.componentId());
        }
        
        signalRepository.save(signal);
    }
    
    /**
     * Create work item when signal threshold is reached
     */
    private void createWorkItem(String workItemId, String componentId, String severity) {
        WorkItem workItem = WorkItem.builder()
            .workItemId(workItemId)
            .componentId(componentId)
            .severity(Severity.valueOf(severity))
            .status(WorkItem.Status.OPEN)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .signalCount(100L) // Threshold
            .build();
        
        workItemRepository.save(workItem);
    }
    
    /**
     * Link all accumulated signals for a component to the work item
     */
    private void linkSignalsToWorkItem(String componentId, String workItemId) {
        // Find all signals for this component that don't have a work item yet
        List<Signal> unlinkedSignals = signalRepository.findByComponentId(componentId)
            .stream()
            .filter(s -> s.getWorkItemId() == null)
            .collect(Collectors.toList());
        
        for (Signal signal : unlinkedSignals) {
            signal.setWorkItemId(workItemId);
        }
        
        signalRepository.saveAll(unlinkedSignals);
    }
    
    /**
     * Signal event for Kafka
     */
    public record SignalEvent(String signalId, SignalRequest signalRequest, String severity) {}
}