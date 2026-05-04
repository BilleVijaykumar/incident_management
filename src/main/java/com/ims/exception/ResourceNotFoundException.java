package com.ims.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a resource is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends ImsException {
    
    public ResourceNotFoundException(String resource, Object identifier) {
        super(String.format("%s not found with identifier: %s", resource, identifier), "RESOURCE_NOT_FOUND");
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}