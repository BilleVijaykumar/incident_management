package com.ims.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when rate limit is exceeded
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends ImsException {
    
    public RateLimitExceededException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED");
    }
    
    public RateLimitExceededException() {
        super("Rate limit exceeded. Please try again later.", "RATE_LIMIT_EXCEEDED");
    }
}