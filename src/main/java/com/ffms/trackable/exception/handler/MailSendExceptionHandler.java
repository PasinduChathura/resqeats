package com.ffms.trackable.exception.handler;

import com.ffms.trackable.common.dto.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class MailSendExceptionHandler {
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({MailSendException.class})
    public ResponseEntity<Object> handleMailSendExceptionHandler(MailSendException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder(exception, HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(StandardResponse.error(errorResponse.getBody()));
    }
}
