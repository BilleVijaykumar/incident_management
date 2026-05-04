package com.ims.worker;

import com.ims.config.KafkaConfig;
import com.ims.service.SignalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Signal Worker - Kafka consumer for async signal processing
 * Provides backpressure handling and resilience
 */
@Component
public class SignalWorker {
    
    private static final Logger log = LoggerFactory.getLogger(SignalWorker.class);
    
    private final SignalService signalService;
    private final ObjectMapper objectMapper;
    
    public SignalWorker(SignalService signalService, ObjectMapper objectMapper) {
        this.signalService = signalService;
        this.objectMapper = objectMapper;
    }
    
    @KafkaListener(
        topics = KafkaConfig.SIGNAL_TOPIC,
        groupId = KafkaConfig.SIGNAL_CONSUMER_GROUP,
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSignal(String message) {
        try {
            log.debug("Received signal from Kafka: {}", message);
            
            SignalService.SignalEvent event = objectMapper.readValue(
                message, 
                SignalService.SignalEvent.class
            );
            
            signalService.processSignalEvent(event);
            
        } catch (Exception e) {
            log.error("Failed to process signal: {}", message, e);
            // Error handler in KafkaConfig will handle retries
            throw new RuntimeException("Signal processing failed", e);
        }
    }
}