package com.ffms.trackable.exception.handler;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.exception.common.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.NOT_FOUND, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleWebClientResponseException(MethodArgumentNotValidException exception) {
        String msg = String.join(", ", exception.getBindingResult().getAllErrors().stream().map(error -> {
            if (error instanceof FieldError)
                return ((FieldError) error).getField() + " " + error.getDefaultMessage();
            else return error.getDefaultMessage();
        }).toList());
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.BAD_REQUEST, msg).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(NotFoundException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.NOT_FOUND, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.FORBIDDEN, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(StandardResponse.error(errorResponse.getBody()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.BAD_REQUEST, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(StandardResponse.error(errorResponse.getBody()));
    }
}