package com.ffms.trackable.exception.handler;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.exception.customer.CustomerAlreadyExistException;
import com.ffms.trackable.exception.customer.CustomerNotFoundException;
import com.ffms.trackable.exception.usermgt.RoleAlreadyExistException;
import com.ffms.trackable.exception.usermgt.RoleNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class CustomerExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({CustomerNotFoundException.class})
    public ResponseEntity<Object> handleCustomerNotFoundException(CustomerNotFoundException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.NOT_FOUND, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({CustomerAlreadyExistException.class})
    public ResponseEntity<Object> handleCustomerAlreadyExistException(CustomerAlreadyExistException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.CONFLICT, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(StandardResponse.error(errorResponse.getBody()));
    }
}
