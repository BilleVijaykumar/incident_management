package com.ims.strategy;

import com.ims.dto.SignalRequest;
import org.springframework.stereotype.Component;

/**
 * Cache Alert Strategy - Medium priority for cache failures
 */
@Component
public class CacheAlertStrategy implements AlertStrategy {
    
    private static final String COMPONENT_TYPE = "CACHE";
    private static final String DEFAULT_PRIORITY = "P2";
    private static final String[] CHANNELS = {"slack", "email"};
    
    @Override
    public String getPriority() {
        return DEFAULT_PRIORITY;
    }
    
    @Override
    public boolean isCritical() {
        return false;
    }
    
    @Override
    public String[] getNotificationChannels() {
        return CHANNELS;
    }
    
    @Override
    public String determineSeverity(SignalRequest signal) {
        String msg = signal.message().toLowerCase();
        
        if (msg.contains("outage") || msg.contains("down") || msg.contains("cluster")) {
            return "P2";
        } else if (msg.contains("eviction") || msg.contains("oom")) {
            return "P2";
        } else if (msg.contains("latency") || msg.contains("slow")) {
            return "P3";
        } else if (msg.contains("miss") || msg.contains("expired")) {
            return "P4";
        }
        return "P3";
    }
    
    @Override
    public String getComponentType() {
        return COMPONENT_TYPE;
    }
}