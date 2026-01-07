package com.ffms.resqeats.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ffms.resqeats.dto.common.ErrorResponse;
import com.ffms.resqeats.exception.common.ErrorCodes;
import com.ffms.resqeats.security.CustomUserDetailsService;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT Authentication Filter.
 * 
 * This filter is responsible for:
 * 1. Parsing JWT from Authorization header
 * 2. Validating the token
 * 3. Creating and setting the ResqeatsSecurityContext (ONCE)
 * 4. Setting Spring Security Authentication
 * 5. Clearing the context after request completion
 * 
 * CRITICAL: This is the ONLY place where SecurityContext is created.
 * No other code should create or modify the security context.
 */
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Generate or retrieve correlation ID for request tracing
        String correlationId = getOrCreateCorrelationId(request);
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        try {
            String jwt = parseJwt(request);
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Parse JWT and create immutable SecurityContext
                ResqeatsSecurityContext securityContext = jwtUtils.parseToken(jwt, correlationId);
                
                // Set custom security context (ONCE, immutable)
                SecurityContextHolder.setContext(securityContext);
                
                // Also set Spring Security context for @PreAuthorize etc.
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().setAuthentication(authentication);
                
                log.debug("Authenticated user: {}, role: {}", 
                        securityContext.getUserId(), securityContext.getRole());
            } else {
                // Set anonymous context for unauthenticated requests
                SecurityContextHolder.setContext(ResqeatsSecurityContext.anonymous(correlationId));
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Authentication error [correlationId={}]: {}", correlationId, e.getMessage(), e);
            
            // Clear any partially set context
            SecurityContextHolder.clearContext();
            
            // Return consistent error response using ErrorResponse
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            ErrorResponse errorResponse = ErrorResponse.fromException(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized",
                    ErrorCodes.AUTH_TOKEN_INVALID,
                    "Authentication failed. Please login again.",
                    request.getServletPath(),
                    correlationId
            );

            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        } finally {
            // CRITICAL: Always clear context after request completion
            SecurityContextHolder.clearContext();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    /**
     * Parse JWT from Authorization header.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    /**
     * Get existing correlation ID from header or create new one.
     */
    private String getOrCreateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
}
