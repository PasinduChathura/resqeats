package com.ffms.resqeats.exception.handler;

import com.ffms.resqeats.common.dto.StandardResponse;
import com.ffms.resqeats.exception.security.TokenExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class PasswordResetTokenExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({TokenExpiredException.class})
    public ResponseEntity<Object> handleTokenExpiredException(TokenExpiredException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.BAD_REQUEST, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponse.error(errorResponse.getBody()));
    }
}
