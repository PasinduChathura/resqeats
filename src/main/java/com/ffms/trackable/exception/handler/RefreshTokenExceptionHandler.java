package com.ffms.trackable.exception.handler;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.exception.security.RefreshTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RefreshTokenExceptionHandler {
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({RefreshTokenException.class})
    public ResponseEntity<Object> handleTokenExpiredException(RefreshTokenException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.UNAUTHORIZED, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(StandardResponse.error(errorResponse.getBody()));
    }
}
