package com.ims.state;

import com.ims.model.WorkItem.Status;
import com.ims.exception.InvalidStateTransitionException;

/**
 * INVESTIGATING State - When team is actively investigating
 */
public class InvestigatingStateHandler implements IncidentStateHandler {
    
    private static final Status[] ALLOWED_TRANSITIONS = {
        Status.OPEN,
        Status.RESOLVED,
        Status.CLOSED
    };
    
    @Override
    public Status getState() {
        return Status.INVESTIGATING;
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
        // Can close without RCA from INVESTIGATING state
        return true;
    }
    
    @Override
    public Status[] getAllowedTransitions() {
        return ALLOWED_TRANSITIONS;
    }
}