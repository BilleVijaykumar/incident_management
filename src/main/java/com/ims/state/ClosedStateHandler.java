package com.ims.state;

import com.ims.model.WorkItem.Status;
import com.ims.exception.InvalidStateTransitionException;
import com.ims.exception.RcaValidationException;

/**
 * CLOSED State - Final state when work item is closed
 * Requires RCA to be complete before closing
 */
public class ClosedStateHandler implements IncidentStateHandler {
    
    private static final Status[] ALLOWED_TRANSITIONS = {
        // Terminal state - no transitions allowed
    };
    
    @Override
    public Status getState() {
        return Status.CLOSED;
    }
    
    @Override
    public boolean canTransitionTo(Status targetState) {
        // Closed is a terminal state - no transitions allowed
        return false;
    }
    
    @Override
    public void transitionTo(Status targetState) {
        throw new InvalidStateTransitionException(
            "CLOSED is a terminal state. No transitions allowed from CLOSED state."
        );
    }
    
    @Override
    public boolean canClose(boolean hasRca) {
        // This is called when trying to enter CLOSED state
        // RCA validation is handled by the state machine
        return hasRca;
    }
    
    @Override
    public Status[] getAllowedTransitions() {
        return ALLOWED_TRANSITIONS;
    }
}