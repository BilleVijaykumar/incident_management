package com.ims.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when RCA validation fails
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RcaValidationException extends ImsException {
    
    public RcaValidationException(String message) {
        super(message, "RCA_VALIDATION_ERROR");
    }
}