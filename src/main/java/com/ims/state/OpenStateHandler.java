package com.ims.state;

import com.ims.model.WorkItem.Status;
import com.ims.exception.InvalidStateTransitionException;

/**
 * OPEN State - Initial state when work item is created
 */
public class OpenStateHandler implements IncidentStateHandler {
    
    private static final Status[] ALLOWED_TRANSITIONS = {
        Status.INVESTIGATING,
        Status.RESOLVED,
        Status.CLOSED
    };
    
    @Override
    public Status getState() {
        return Status.OPEN;
    }
    
    @Override
    public boolean canTransitionTo(Status targetState) {
        for (Status allowed : ALLOWED_TRANSITIONS) {
            if (allowed == targetState) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void transitionTo(Status targetState) {
        if (!canTransitionTo(targetState)) {
            throw new InvalidStateTransitionException(getState().name(), targetState.name());
        }
    }
    
    @Override
    public boolean canClose(boolean hasRca) {
        // Can close without RCA from OPEN state
        return true;
    }
    
    @Override
    public Status[] getAllowedTransitions() {
        return ALLOWED_TRANSITIONS;
    }
}