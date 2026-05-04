package com.ims.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import com.ims.dto.ApiResponse;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler for the IMS application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(ex.getMessage(), ex.getErrorCode());
    }
    
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ApiResponse<?> handleInvalidStateTransition(InvalidStateTransitionException ex) {
        return ApiResponse.error(ex.getMessage(), ex.getErrorCode());
    }
    
    @ExceptionHandler(RcaValidationException.class)
    public ApiResponse<?> handleRcaValidation(RcaValidationException ex) {
        return ApiResponse.error(ex.getMessage(), ex.getErrorCode());
    }
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ApiResponse<?> handleRateLimitExceeded(RateLimitExceededException ex) {
        return ApiResponse.error(ex.getMessage(), ex.getErrorCode());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ApiResponse.error("Validation failed: " + errors, "VALIDATION_ERROR");
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ApiResponse.error("Data integrity violation: " + ex.getMostSpecificCause().getMessage(), 
                                "DATA_INTEGRITY_ERROR");
    }
    
    @ExceptionHandler(ImsException.class)
    public ApiResponse<?> handleImsException(ImsException ex) {
        return ApiResponse.error(ex.getMessage(), ex.getErrorCode());
    }
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleGenericException(Exception ex) {
        return ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR");
    }
}