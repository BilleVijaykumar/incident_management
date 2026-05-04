package com.ims.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base exception for IMS application
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ImsException extends RuntimeException {
    private final String errorCode;
    
    public ImsException(String message) {
        super(message);
        this.errorCode = "IMS_ERROR";
    }
    
    public ImsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ImsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "IMS_ERROR";
    }
    
    public ImsException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}