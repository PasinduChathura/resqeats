package com.ffms.trackable.exception.handler;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.exception.usermgt.RoleAlreadyExistException;
import com.ffms.trackable.exception.usermgt.RoleNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RoleExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({RoleNotFoundException.class})
    public ResponseEntity<Object> handleRoleNotFoundException(RoleNotFoundException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.NOT_FOUND, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({RoleAlreadyExistException.class})
    public ResponseEntity<Object> handleRoleAlreadyExistException(RoleAlreadyExistException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.CONFLICT, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(StandardResponse.error(errorResponse.getBody()));
    }
}
