package com.ims.state;

import com.ims.model.WorkItem.Status;
import com.ims.exception.InvalidStateTransitionException;

/**
 * State Pattern - Work Item State Interface
 * Defines the contract for all work item states
 */
public interface IncidentStateHandler {
    
    /**
     * Get the current state
     */
    Status getState();
    
    /**
     * Check if transition to target state is allowed
     */
    boolean canTransitionTo(Status targetState);
    
    /**
     * Perform the transition to target state
     */
    void transitionTo(Status targetState);
    
    /**
     * Validate if work item can be closed (requires RCA)
     */
    boolean canClose(boolean hasRca);
    
    /**
     * Get allowed next states
     */
    Status[] getAllowedTransitions();
}

// Concrete State Implementations