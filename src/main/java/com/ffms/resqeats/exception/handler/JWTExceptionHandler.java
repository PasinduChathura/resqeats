package com.ffms.resqeats.exception.handler;

import com.ffms.resqeats.common.dto.StandardResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
public class JWTExceptionHandler {
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({MalformedJwtException.class})
    public ResponseEntity<Object> handleMalformedJwtException(MalformedJwtException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.UNAUTHORIZED, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({ExpiredJwtException.class})
    public ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.UNAUTHORIZED, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({UnsupportedJwtException.class})
    public ResponseEntity<Object> handleUnsupportedJwtException(UnsupportedJwtException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.UNAUTHORIZED, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({SignatureException.class})
    public ResponseEntity<Object> handleSignatureException(SignatureException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.UNAUTHORIZED, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(StandardResponse.error(errorResponse.getBody()));
    }
}
