package com.ims.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Debounce Manager using Redis TTL
 * Prevents creating duplicate incidents for the same component within debounce window
 */
@Component
public class DebounceManager {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String DEBOUNCE_KEY_PREFIX = "debounce:";
    private static final String SIGNAL_COUNT_KEY_PREFIX = "debounce:count:";
    private static final String FIRST_SIGNAL_KEY_PREFIX = "debounce:first:";
    
    private final Duration debounceWindow;
    private final int maxSignalsInWindow;
    
    public DebounceManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.debounceWindow = Duration.ofSeconds(10);
        this.maxSignalsInWindow = 100;
    }
    
    /**
     * Check if we should create a work item for this component
     * Returns the work item ID if threshold reached, null if accumulating
     */
    public String checkAndCreateWorkItem(String componentId, String severity) {
        String countKey = SIGNAL_COUNT_KEY_PREFIX + componentId;
        String firstSignalKey = FIRST_SIGNAL_KEY_PREFIX + componentId;
        String severityKey = "debounce:severity:" + componentId;
        
        // Increment signal count
        Long count = redisTemplate.opsForValue().increment(countKey);
        
        if (count == 1) {
            // First signal - set TTL and severity
            redisTemplate.opsForValue().set(firstSignalKey, 
                String.valueOf(Instant.now().toEpochMilli()), 
                debounceWindow);
            redisTemplate.opsForValue().set(severityKey, severity, debounceWindow);
            redisTemplate.expire(countKey, debounceWindow);
        }
        
        // Check if we've reached the threshold
        if (count >= maxSignalsInWindow) {
            // Threshold reached - create work item
            String workItemId = "WI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Clear debounce state
            clearDebounce(componentId);
            
            return workItemId;
        }
        
        return null; // Still accumulating
    }
    
    /**
     * Get signal count for component in current debounce window
     */
    public long getSignalCount(String componentId) {
        String countKey = SIGNAL_COUNT_KEY_PREFIX + componentId;
        String value = redisTemplate.opsForValue().get(countKey);
        return value != null ? Long.parseLong(value) : 0;
    }
    
    /**
     * Get first signal timestamp for component
     */
    public Instant getFirstSignalTime(String componentId) {
        String firstSignalKey = FIRST_SIGNAL_KEY_PREFIX + componentId;
        String value = redisTemplate.opsForValue().get(firstSignalKey);
        if (value != null) {
            return Instant.ofEpochMilli(Long.parseLong(value));
        }
        return null;
    }
    
    /**
     * Clear debounce state for component (e.g., when work item is created)
     */
    public void clearDebounce(String componentId) {
        redisTemplate.delete(DEBOUNCE_KEY_PREFIX + componentId);
        redisTemplate.delete(SIGNAL_COUNT_KEY_PREFIX + componentId);
        redisTemplate.delete(FIRST_SIGNAL_KEY_PREFIX + componentId);
        redisTemplate.delete("debounce:severity:" + componentId);
    }
    
    /**
     * Check if component is in debounce window
     */
    public boolean isInDebounceWindow(String componentId) {
        String debounceKey = DEBOUNCE_KEY_PREFIX + componentId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(debounceKey));
    }
}