package com.ffms.resqeats.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized API response wrapper for consistent response format across all endpoints.
 * This class provides a uniform structure for both successful and error responses.
 * 
 * @deprecated Use {@link ApiResponse} instead for new implementations.
 * This class is kept for backward compatibility and will delegate to ApiResponse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated
public class StandardResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    public static <T> StandardResponse<T> empty() {
        return success(null);
    }

    public static <T> StandardResponse<T> success(T data) {
        return StandardResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> StandardResponse<T> success(T data, String message) {
        return StandardResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> StandardResponse<T> error(String message) {
        return StandardResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> StandardResponse<T> error(String message, String errorCode) {
        return StandardResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    /**
     * @deprecated Use {@link #error(String)} instead
     */
    @Deprecated
    public static <T> StandardResponse<T> error(T data) {
        return StandardResponse.<T>builder()
                .success(false)
                .message("ERROR!")
                .data(data)
                .build();
    }
}