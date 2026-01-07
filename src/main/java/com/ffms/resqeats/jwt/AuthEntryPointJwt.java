package com.ffms.resqeats.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ffms.resqeats.dto.common.ErrorResponse;
import com.ffms.resqeats.exception.common.ErrorCodes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication entry point for handling unauthorized access attempts.
 * Returns consistent error responses using the ErrorResponse DTO.
 */
@Slf4j
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        int statusCode;
        String message;
        String errorCode;

        if (authException instanceof AuthenticationCredentialsNotFoundException) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            message = "Invalid or missing credentials";
            errorCode = ErrorCodes.AUTH_INVALID_CREDENTIALS;
        } else if (authException instanceof BadCredentialsException) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            message = "Incorrect username or password";
            errorCode = ErrorCodes.AUTH_INVALID_CREDENTIALS;
        } else if (authException instanceof AccountStatusException) {
            statusCode = HttpServletResponse.SC_FORBIDDEN;
            message = "Account is disabled or locked";
            errorCode = ErrorCodes.AUTH_ACCESS_DENIED;
        } else if (authException instanceof InternalAuthenticationServiceException) {
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            message = "Internal authentication server error";
            errorCode = ErrorCodes.SYSTEM_INTERNAL_ERROR;
            log.error("Internal authentication error: {}", authException.getMessage(), authException);
        } else if (authException instanceof InsufficientAuthenticationException) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            message = "Full authentication is required to access this resource";
            errorCode = ErrorCodes.AUTH_TOKEN_INVALID;
        } else {
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            message = "Internal server error";
            errorCode = ErrorCodes.SYSTEM_INTERNAL_ERROR;
            log.error("Unexpected authentication error: {}", authException.getMessage(), authException);
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(statusCode);

        String correlationId = MDC.get(CORRELATION_ID_KEY);
        ErrorResponse errorResponse = ErrorResponse.fromException(
                statusCode,
                statusCode == HttpServletResponse.SC_UNAUTHORIZED ? "Unauthorized" : 
                    statusCode == HttpServletResponse.SC_FORBIDDEN ? "Forbidden" : "Internal Server Error",
                errorCode,
                message,
                request.getServletPath(),
                correlationId
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
