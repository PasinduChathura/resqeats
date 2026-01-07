package com.ffms.resqeats.exception.security;

/**
 * Base class for all security-related exceptions.
 */
public abstract class SecurityException extends RuntimeException {

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get error code for API response.
     */
    public abstract String getErrorCode();
}
