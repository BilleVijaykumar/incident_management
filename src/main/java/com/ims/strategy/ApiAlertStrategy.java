package com.ims.strategy;

import com.ims.dto.SignalRequest;
import org.springframework.stereotype.Component;

/**
 * API Alert Strategy - Default strategy for API failures
 */
@Component
public class ApiAlertStrategy implements AlertStrategy {
    
    private static final String COMPONENT_TYPE = "API";
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
        
        if (msg.contains("outage") || msg.contains("down") || msg.contains("unavailable")) {
            return "P1";
        } else if (msg.contains("5xx") || msg.contains("error")) {
            return "P2";
        } else if (msg.contains("4xx") || msg.contains("timeout")) {
            return "P3";
        } else if (msg.contains("latency") || msg.contains("slow")) {
            return "P3";
        }
        return "P3";
    }
    
    @Override
    public String getComponentType() {
        return COMPONENT_TYPE;
    }
}