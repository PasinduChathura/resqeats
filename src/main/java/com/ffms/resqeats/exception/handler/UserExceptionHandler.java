package com.ffms.resqeats.exception.handler;

import com.ffms.resqeats.common.dto.StandardResponse;
import com.ffms.resqeats.exception.usermgt.UserAlreadyExistException;
import com.ffms.resqeats.exception.usermgt.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class UserExceptionHandler {
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({UserAlreadyExistException.class})
    public ResponseEntity<Object> handleUserConflictException(UserAlreadyExistException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.CONFLICT, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.NOT_FOUND, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(StandardResponse.error(errorResponse.getBody()));
    }
}