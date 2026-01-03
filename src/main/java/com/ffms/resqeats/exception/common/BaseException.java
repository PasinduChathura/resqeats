package com.ffms.resqeats.exception.common;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Base exception class for all application business exceptions.
 * Provides consistent error structure with error codes, timestamps, and details.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final String errorCategory;
    private final LocalDateTime timestamp;
    private final String details;

    protected BaseException(String message, String errorCode, String errorCategory) {
        super(message);
        this.errorCode = errorCode;
        this.errorCategory = errorCategory;
        this.timestamp = LocalDateTime.now();
        this.details = null;
    }

    protected BaseException(String message, String errorCode, String errorCategory, String details) {
        super(message);
        this.errorCode = errorCode;
        this.errorCategory = errorCategory;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    protected BaseException(String message, String errorCode, String errorCategory, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorCategory = errorCategory;
        this.timestamp = LocalDateTime.now();
        this.details = cause != null ? cause.getMessage() : null;
    }

    protected BaseException(String message, String errorCode, String errorCategory, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorCategory = errorCategory;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    /**
     * Returns a user-friendly error message suitable for client display.
     */
    public String getUserMessage() {
        return getMessage();
    }

    /**
     * Returns a detailed technical message for logging.
     */
    public String getTechnicalMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorCode).append("] ").append(getMessage());
        if (details != null && !details.isEmpty()) {
            sb.append(" - Details: ").append(details);
        }
        return sb.toString();
    }
}
