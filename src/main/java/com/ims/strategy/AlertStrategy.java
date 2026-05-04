package com.ims.strategy;

import com.ims.dto.SignalRequest;

/**
 * Alert Strategy Interface - Strategy Pattern
 * Defines contract for different alerting strategies based on component type
 */
public interface AlertStrategy {
    
    /**
     * Get the priority/severity for this alert type
     */
    String getPriority();
    
    /**
     * Determine if this alert requires immediate attention
     */
    boolean isCritical();
    
    /**
     * Get notification channels for this alert type
     */
    String[] getNotificationChannels();
    
    /**
     * Process the signal and return enhanced severity
     */
    String determineSeverity(SignalRequest signal);
    
    /**
     * Get the component type this strategy handles
     */
    String getComponentType();
}