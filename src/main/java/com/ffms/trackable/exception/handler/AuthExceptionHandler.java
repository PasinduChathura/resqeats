package com.ffms.trackable.exception.handler;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.exception.usermgt.InvalidOldPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class AuthExceptionHandler {
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.UNAUTHORIZED, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidOldPasswordException.class})
    public ResponseEntity<Object> handleInvalidOldPasswordException(InvalidOldPasswordException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.BAD_REQUEST, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponse.error(errorResponse.getBody()));
    }
}
