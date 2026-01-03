package com.ffms.trackable.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffms.trackable.common.dto.StandardResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        int statusCode;
        String message;

        if (authException instanceof AuthenticationCredentialsNotFoundException) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            message = "Invalid or missing credentials";
        } else if (authException instanceof BadCredentialsException) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            message = "Incorrect username or password";
        } else if (authException instanceof AccountStatusException) {
            statusCode = HttpServletResponse.SC_FORBIDDEN;
            message = "Account is disabled or locked";
        } else if (authException instanceof InternalAuthenticationServiceException) {
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            message = "Internal authentication server error";
        } else if (authException instanceof InsufficientAuthenticationException) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            message = "Full authentication is required to access this resource";
        } else {
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            message = "Internal server error";
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(statusCode);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", statusCode);
        body.put("message", message);
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), StandardResponse.error(body));
    }
}
