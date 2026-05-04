package com.ims.strategy;

import com.ims.dto.SignalRequest;
import org.springframework.stereotype.Component;

/**
 * Queue Alert Strategy - High priority for async queue failures
 */
@Component
public class QueueAlertStrategy implements AlertStrategy {
    
    private static final String COMPONENT_TYPE = "QUEUE";
    private static final String DEFAULT_PRIORITY = "P1";
    private static final String[] CHANNELS = {"slack-critical", "email", "pagerduty"};
    
    @Override
    public String getPriority() {
        return DEFAULT_PRIORITY;
    }
    
    @Override
    public boolean isCritical() {
        return true;
    }
    
    @Override
    public String[] getNotificationChannels() {
        return CHANNELS;
    }
    
    @Override
    public String determineSeverity(SignalRequest signal) {
        String msg = signal.message().toLowerCase();
        
        if (msg.contains("outage") || msg.contains("down") || msg.contains("broker")) {
            return "P1";
        } else if (msg.contains("deadletter") || msg.contains("dlq")) {
            return "P1";
        } else if (msg.contains("lag") || msg.contains("backlog")) {
            return "P2";
        } else if (msg.contains("timeout") || msg.contains("retry")) {
            return "P2";
        }
        return "P2";
    }
    
    @Override
    public String getComponentType() {
        return COMPONENT_TYPE;
    }
}