package com.ims.strategy;

import com.ims.dto.SignalRequest;
import org.springframework.stereotype.Component;

/**
 * RDBMS Alert Strategy - Highest priority for database failures
 */
@Component
public class RdbmsAlertStrategy implements AlertStrategy {
    
    private static final String COMPONENT_TYPE = "RDBMS";
    private static final String DEFAULT_PRIORITY = "P0";
    private static final String[] CHANNELS = {"pagerduty", "slack-critical", "email", "sms"};
    
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
        
        // Escalate based on keywords
        if (msg.contains("outage") || msg.contains("down") || msg.contains("unavailable")) {
            return "P0";
        } else if (msg.contains("corruption") || msg.contains("data loss")) {
            return "P0";
        } else if (msg.contains("replication") || msg.contains("lag")) {
            return "P1";
        } else if (msg.contains("slow") || msg.contains("latency")) {
            return "P2";
        }
        return "P1"; // Default for RDBMS
    }
    
    @Override
    public String getComponentType() {
        return COMPONENT_TYPE;
    }
}