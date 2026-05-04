package com.ims.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an invalid state transition is attempted
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStateTransitionException extends ImsException {
    
    public InvalidStateTransitionException(String currentState, String targetState) {
        super(String.format("Invalid state transition from %s to %s", currentState, targetState), 
              "INVALID_STATE_TRANSITION");
    }
    
    public InvalidStateTransitionException(String message) {
        super(message, "INVALID_STATE_TRANSITION");
    }
}