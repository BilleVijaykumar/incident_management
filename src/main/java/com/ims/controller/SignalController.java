package com.ims.controller;

import com.ims.dto.ApiResponse;
import com.ims.dto.SignalRequest;
import com.ims.service.SignalService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Signal Controller - High-throughput signal ingestion API
 * POST /signals
 */
@RestController
@RequestMapping("/api/signals")
public class SignalController {
    
    private static final Logger log = LoggerFactory.getLogger(SignalController.class);
    
    private final SignalService signalService;
    
    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }
    
    /**
     * Ingest a signal - async, non-blocking
     * POST /signals
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> ingestSignal(
            @Valid @RequestBody SignalRequest request) {
        
        log.info("Received signal for component: {}", request.componentId());
        
        String signalId = signalService.ingestSignal(request);
        
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(signalId, "Signal accepted for processing"));
    }
    
    /**
     * Batch ingest signals
     * POST /signals/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<String>> ingestSignalsBatch(
            @Valid @RequestBody List<SignalRequest> requests) {
        
        log.info("Received batch of {} signals", requests.size());
        
        List<String> signalIds = new ArrayList<>();
        for (SignalRequest request : requests) {
            try {
                String signalId = signalService.ingestSignal(request);
                signalIds.add(signalId);
            } catch (Exception e) {
                log.warn("Failed to ingest signal: {}", e.getMessage());
            }
        }
        
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(
                "Processed " + signalIds.size() + " signals",
                "Batch signal ingestion complete"
            ));
    }
}