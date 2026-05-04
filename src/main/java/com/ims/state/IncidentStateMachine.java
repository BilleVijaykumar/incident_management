package com.ims.state;

import com.ims.model.WorkItem.Status;
import com.ims.exception.InvalidStateTransitionException;
import com.ims.exception.RcaValidationException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * State Machine - Manages work item state transitions
 * Implements the State Pattern
 */
@Component
public class IncidentStateMachine {
    
    private final Map<Status, IncidentStateHandler> handlers;
    
    public IncidentStateMachine(
            OpenStateHandler openHandler,
            InvestigatingStateHandler investigatingHandler,
            ResolvedStateHandler resolvedHandler,
            ClosedStateHandler closedHandler) {
        
        this.handlers = new EnumMap<>(Status.class);
        handlers.put(Status.OPEN, openHandler);
        handlers.put(Status.INVESTIGATING, investigatingHandler);
        handlers.put(Status.RESOLVED, resolvedHandler);
        handlers.put(Status.CLOSED, closedHandler);
    }
    
    public void validateTransition(Status currentState, Status targetState) {
        IncidentStateHandler handler = handlers.get(currentState);
        if (handler == null) {
            throw new InvalidStateTransitionException(currentState.name(), targetState.name());
        }
        handler.transitionTo(targetState);
    }
    
    /**
     * Check if transition is allowed
     */
    public boolean canTransition(Status currentState, Status targetState) {
        IncidentStateHandler handler = handlers.get(currentState);
        if (handler == null) {
            return false;
        }
        return handler.canTransitionTo(targetState);
    }
    
    /**
     * Validate close request - requires RCA for CLOSED state
     */
    public void validateCloseRequest(Status currentState, boolean hasRca) {
        if (currentState == Status.CLOSED) {
            return; // Already closed
        }
        
        IncidentStateHandler handler = handlers.get(currentState);
        if (handler != null && !handler.canClose(hasRca)) {
            throw new RcaValidationException(
                "Cannot close work item without complete RCA. " +
                "Please submit Root Cause Analysis first."
            );
        }
    }
    
    /**
     * Get allowed transitions for current state
     */
    public Status[] getAllowedTransitions(Status currentState) {
        IncidentStateHandler handler = handlers.get(currentState);
        if (handler == null) {
            return new Status[0];
        }
        return handler.getAllowedTransitions();
    }
    
    /**
     * Get handler for state
     */
    public IncidentStateHandler getHandler(Status state) {
        return handlers.get(state);
    }
}