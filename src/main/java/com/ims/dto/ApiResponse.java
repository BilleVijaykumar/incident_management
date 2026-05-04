package com.ims.dto;

import java.time.Instant;

/**
 * ApiResponse DTO - Standard API response wrapper
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    Instant timestamp,
    String errorCode
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, Instant.now(), null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Instant.now(), null);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now(), "ERROR");
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, Instant.now(), errorCode);
    }
}