package com.ims.strategy;

import com.ims.dto.SignalRequest;
import org.springframework.stereotype.Component;

/**
 * NoSQL Alert Strategy - For NoSQL store failures
 */
@Component
public class NoSqlAlertStrategy implements AlertStrategy {
    
    private static final String COMPONENT_TYPE = "NOSQL";
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
        
        if (msg.contains("outage") || msg.contains("down") || msg.contains("unavailable")) {
            return "P1";
        } else if (msg.contains("shard") || msg.contains("cluster")) {
            return "P1";
        } else if (msg.contains("latency") || msg.contains("slow")) {
            return "P2";
        } else if (msg.contains("consistency") || msg.contains("stale")) {
            return "P2";
        }
        return "P2";
    }
    
    @Override
    public String getComponentType() {
        return COMPONENT_TYPE;
    }
}