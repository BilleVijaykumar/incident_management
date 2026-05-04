package com.ims.metrics;

import com.ims.cache.DashboardCache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics Service - Throughput and performance monitoring
 * Logs signals/sec every 5 seconds
 */
@Component
public class MetricsService {
    
    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);
    
    private final DashboardCache dashboardCache;
    private final MeterRegistry meterRegistry;
    
    private final AtomicLong signalCount = new AtomicLong(0);
    private final AtomicLong incidentCount = new AtomicLong(0);
    private final Counter signalsReceivedCounter;
    private final Counter signalsProcessedCounter;
    private final Timer signalProcessingTimer;
    
    public MetricsService(DashboardCache dashboardCache, MeterRegistry meterRegistry) {
        this.dashboardCache = dashboardCache;
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.signalsReceivedCounter = Counter.builder("ims.signals.received")
            .description("Total signals received")
            .register(meterRegistry);
        
        this.signalsProcessedCounter = Counter.builder("ims.signals.processed")
            .description("Total signals processed")
            .register(meterRegistry);
        
        this.signalProcessingTimer = Timer.builder("ims.signals.processing.time")
            .description("Signal processing time")
            .register(meterRegistry);
        
        // Register gauges
        Gauge.builder("ims.signals.current", signalCount, AtomicLong::get)
            .description("Current signal count")
            .register(meterRegistry);
        
        Gauge.builder("ims.incidents.current", incidentCount, AtomicLong::get)
            .description("Current incident count")
            .register(meterRegistry);
    }
    
    /**
     * Record signal received
     */
    public void recordSignalReceived() {
        signalsReceivedCounter.increment();
        signalCount.incrementAndGet();
    }
    
    /**
     * Record signal processed
     */
    public void recordSignalProcessed() {
        signalsProcessedCounter.increment();
    }
    
    /**
     * Record processing time
     */
    public Timer.Sample startProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopProcessingTimer(Timer.Sample sample) {
        sample.stop(signalProcessingTimer);
    }
    
    /**
     * Update incident count
     */
    public void updateIncidentCount(long count) {
        incidentCount.set(count);
    }
    
    /**
     * Log throughput every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void logThroughput() {
        long currentSignals = signalCount.get();
        long signalsProcessed = (long) signalsProcessedCounter.count();
        
        if (currentSignals > 0 || signalsProcessed > 0) {
            double signalsPerSec = currentSignals / 5.0;
            log.info("=== METRICS === signals/sec: {:.2f} | total received: {} | total processed: {}",
                signalsPerSec, 
                (long) signalsReceivedCounter.count(),
                signalsProcessed);
        }
    }
    
    /**
     * Get current throughput
     */
    public double getCurrentThroughput() {
        return signalCount.get() / 5.0;
    }
}