package com.ffms.resqeats.exception.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when user has insufficient role for an operation.
 * Results in HTTP 403 Forbidden.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class InsufficientRoleException extends SecurityException {

    public InsufficientRoleException(String message) {
        super(message);
    }

    public InsufficientRoleException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return "SEC_004";
    }
}
