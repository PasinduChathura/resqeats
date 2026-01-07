package com.ffms.resqeats.exception.handler;

import com.ffms.resqeats.common.logging.AppLogger;
import com.ffms.resqeats.common.logging.CorrelationIdFilter;
import com.ffms.resqeats.dto.common.ErrorResponse;
import com.ffms.resqeats.exception.cart.CartException;
import com.ffms.resqeats.exception.common.BaseException;
import com.ffms.resqeats.exception.common.ErrorCodes;
import com.ffms.resqeats.exception.common.NotFoundException;
import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.exception.order.OrderException;
import com.ffms.resqeats.exception.payment.PaymentException;
import com.ffms.resqeats.exception.security.RefreshTokenException;
import com.ffms.resqeats.exception.security.TokenExpiredException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler providing consistent error responses with logging.
 * Per SRS Section 7.4: All exceptions are logged with correlation IDs for request tracing.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final AppLogger appLogger = AppLogger.of(log);

    // ===================== Standard HTTP Exceptions =====================

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        appLogger.warn("Endpoint not found: {} {}", request.getMethod(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", 
                ErrorCodes.SYSTEM_INTERNAL_ERROR, 
                "The requested endpoint does not exist: " + request.getRequestURI(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fe) {
                        return ErrorResponse.FieldError.builder()
                                .field(fe.getField())
                                .message(error.getDefaultMessage())
                                .rejectedValue(fe.getRejectedValue())
                                .build();
                    }
                    return ErrorResponse.FieldError.builder()
                            .field("unknown")
                            .message(error.getDefaultMessage())
                            .build();
                })
                .collect(Collectors.toList());

        String message = fieldErrors.stream()
                .map(fe -> fe.getField() + ": " + fe.getMessage())
                .collect(Collectors.joining("; "));

        appLogger.warn("Validation failed: {}", message);

        ErrorResponse errorResponse = ErrorResponse.forValidation(
                "Validation failed", request.getRequestURI(), getCorrelationId(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        appLogger.warn("Missing required parameter: {}", ex.getParameterName());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ErrorCodes.VALIDATION_REQUIRED_FIELD,
                "Required parameter is missing: " + ex.getParameterName(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        appLogger.warn("Type mismatch: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ErrorCodes.VALIDATION_INVALID_FORMAT, message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        appLogger.warn("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ErrorCodes.VALIDATION_FAILED, ex.getMessage(), request);
    }

    // ===================== Security Exceptions =====================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        appLogger.logSecurityEvent("ACCESS_DENIED", request.getRequestURI());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden",
                ErrorCodes.AUTH_ACCESS_DENIED, 
                "You don't have permission to access this resource", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        appLogger.logSecurityEvent("BAD_CREDENTIALS", "Authentication failed");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                ErrorCodes.AUTH_INVALID_CREDENTIALS,
                "Invalid username or password", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        appLogger.logSecurityEvent("AUTH_FAILED", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                ErrorCodes.AUTH_INVALID_CREDENTIALS, 
                "Authentication failed. Please check your credentials.", request);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenException(
            RefreshTokenException ex, HttpServletRequest request) {
        appLogger.warn("Refresh token error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                ErrorCodes.AUTH_REFRESH_TOKEN_INVALID, 
                "Session refresh failed. Please login again.", request);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(
            TokenExpiredException ex, HttpServletRequest request) {
        appLogger.warn("Token expired: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                ErrorCodes.AUTH_TOKEN_EXPIRED, 
                "Your session has expired. Please login again.", request);
    }

    // ===================== Business Exceptions =====================

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex, HttpServletRequest request) {
        appLogger.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found",
                ErrorCodes.SYSTEM_INTERNAL_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        appLogger.warn("[{}] Business error: {}", ex.getErrorCode(), ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(CartException.class)
    public ResponseEntity<ErrorResponse> handleCartException(
            CartException ex, HttpServletRequest request) {
        return handleBaseException(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(
            OrderException ex, HttpServletRequest request) {
        return handleBaseException(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(
            PaymentException ex, HttpServletRequest request) {
        appLogger.logError("PAYMENT", "Payment", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ex.getErrorCode(), ex.getMessage(), request);
    }

    // ===================== JWT Exceptions =====================

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwtException(
            MalformedJwtException ex, HttpServletRequest request) {
        appLogger.logSecurityEvent("MALFORMED_JWT", "Invalid JWT token format");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                ErrorCodes.AUTH_TOKEN_INVALID,
                "Authentication failed. Please login again.", request);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(
            ExpiredJwtException ex, HttpServletRequest request) {
        String subject = ex.getClaims() != null ? ex.getClaims().getSubject() : "unknown";
        appLogger.warn("JWT token expired for user: {}", subject);
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                ErrorCodes.AUTH_TOKEN_EXPIRED,
                "Your session has expired. Please login again.", request);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedJwtException(
            UnsupportedJwtException ex, HttpServletRequest request) {
        appLogger.logSecurityEvent("UNSUPPORTED_JWT", "Unsupported JWT token");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                ErrorCodes.AUTH_TOKEN_INVALID,
                "Authentication failed. Please login again.", request);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponse> handleSignatureException(
            SignatureException ex, HttpServletRequest request) {
        appLogger.logSecurityEvent("INVALID_JWT_SIGNATURE", "JWT signature validation failed");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                ErrorCodes.AUTH_TOKEN_INVALID,
                "Authentication failed. Please login again.", request);
    }

    // ===================== Mail Exception =====================

    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<ErrorResponse> handleMailSendException(
            MailSendException ex, HttpServletRequest request) {
        appLogger.error("Mail send error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                ErrorCodes.NOTIFICATION_SEND_FAILED,
                "Failed to send email. Please try again later.", request);
    }

    // ===================== Catch-All Exception Handler =====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        appLogger.error("Unhandled exception", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                ErrorCodes.SYSTEM_INTERNAL_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    // ===================== Helper Methods =====================

    private ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, HttpServletRequest request, HttpStatus status) {
        appLogger.warn("[{}] {}: {}", ex.getErrorCode(), ex.getErrorCategory(), ex.getMessage());
        
        HttpStatus resolvedStatus = resolveStatus(ex.getErrorCode(), status);
        
        return buildErrorResponse(resolvedStatus, resolvedStatus.getReasonPhrase(),
                ex.getErrorCode(), ex.getMessage(), request);
    }

    private HttpStatus resolveStatus(String errorCode, HttpStatus defaultStatus) {
        if (errorCode == null) return defaultStatus;
        
        // Not found errors
        if (errorCode.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }
        // Access denied errors
        if (errorCode.contains("ACCESS_DENIED")) {
            return HttpStatus.FORBIDDEN;
        }
        // Auth errors
        if (errorCode.startsWith("AUTH_")) {
            return HttpStatus.UNAUTHORIZED;
        }
        return defaultStatus;
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, String error, String errorCode, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.fromException(
                status.value(),
                error,
                errorCode,
                message,
                request.getRequestURI(),
                getCorrelationId()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    private String getCorrelationId() {
        return MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY);
    }
}