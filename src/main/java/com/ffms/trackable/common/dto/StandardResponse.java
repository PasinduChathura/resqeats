package com.ffms.trackable.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StandardResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> StandardResponse<T> empty() {
        return success(null);
    }

    public static <T> StandardResponse<T> success(T data) {
        return StandardResponse.<T>builder()
                .message("SUCCESS!")
                .data(data)
                .success(true)
                .build();
    }

    public static <T> StandardResponse<T> error(T data) {
        return StandardResponse.<T>builder()
                .message("ERROR!")
                .data(data)
                .success(false)
                .build();
    }
}