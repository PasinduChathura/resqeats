package com.ffms.resqeats.common.logging;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Centralized logging utility providing consistent logging patterns across the application.
 * Supports structured logging with correlation IDs, timing, and context.
 */
public final class AppLogger {

    private static final String CORRELATION_ID = "correlationId";
    private static final String USER_ID = "userId";
    private static final String OPERATION = "operation";
    private static final String ENTITY_TYPE = "entityType";
    private static final String ENTITY_ID = "entityId";

    private final Logger logger;
    private final String className;

    private AppLogger(Logger logger) {
        this.logger = logger;
        this.className = logger.getName().substring(logger.getName().lastIndexOf('.') + 1);
    }

    /**
     * Creates an AppLogger instance wrapping the provided SLF4J logger.
     */
    public static AppLogger of(Logger logger) {
        return new AppLogger(logger);
    }

    // ===================== Correlation ID Management =====================

    /**
     * Sets a correlation ID in MDC for request tracing.
     */
    public static String setCorrelationId() {
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(CORRELATION_ID, correlationId);
        return correlationId;
    }

    /**
     * Sets an existing correlation ID in MDC.
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID, correlationId);
    }

    /**
     * Gets the current correlation ID.
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID);
    }

    /**
     * Clears logging context.
     */
    public static void clearContext() {
        MDC.clear();
    }

    /**
     * Sets user context for logging.
     */
    public static void setUserContext(Long userId) {
        if (userId != null) {
            MDC.put(USER_ID, String.valueOf(userId));
        }
    }

    // ===================== Info Logging =====================

    /**
     * Logs an info message for operation start.
     */
    public void logStart(String operation, String entityType) {
        MDC.put(OPERATION, operation);
        MDC.put(ENTITY_TYPE, entityType);
        logger.info("[{}] Starting {} operation for {}", className, operation, entityType);
    }

    /**
     * Logs an info message for operation start with entity ID.
     */
    public void logStart(String operation, String entityType, Object entityId) {
        MDC.put(OPERATION, operation);
        MDC.put(ENTITY_TYPE, entityType);
        if (entityId != null) {
            MDC.put(ENTITY_ID, String.valueOf(entityId));
        }
        logger.info("[{}] Starting {} operation for {} with id={}", className, operation, entityType, entityId);
    }

    /**
     * Logs successful completion of an operation.
     */
    public void logSuccess(String operation, String entityType) {
        logger.info("[{}] Completed {} operation for {} successfully", className, operation, entityType);
    }

    /**
     * Logs successful completion with entity ID.
     */
    public void logSuccess(String operation, String entityType, Object entityId) {
        logger.info("[{}] Completed {} operation for {} with id={} successfully", 
                className, operation, entityType, entityId);
    }

    /**
     * Logs successful completion with additional details.
     */
    public void logSuccess(String operation, String entityType, Object entityId, String details) {
        logger.info("[{}] Completed {} operation for {} with id={} - {}", 
                className, operation, entityType, entityId, details);
    }

    /**
     * Generic info log.
     */
    public void info(String message, Object... args) {
        logger.info("[{}] " + message, prependClassName(args));
    }

    // ===================== Debug Logging =====================

    /**
     * Debug level logging.
     */
    public void debug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] " + message, prependClassName(args));
        }
    }

    // ===================== Warning Logging =====================

    /**
     * Logs a warning message.
     */
    public void warn(String message, Object... args) {
        logger.warn("[{}] " + message, prependClassName(args));
    }

    /**
     * Logs a warning for a specific operation.
     */
    public void logWarning(String operation, String entityType, String reason) {
        logger.warn("[{}] Warning during {} operation for {}: {}", className, operation, entityType, reason);
    }

    /**
     * Logs a warning with entity ID.
     */
    public void logWarning(String operation, String entityType, Object entityId, String reason) {
        logger.warn("[{}] Warning during {} operation for {} with id={}: {}", 
                className, operation, entityType, entityId, reason);
    }

    // ===================== Error Logging =====================

    /**
     * Logs an error message.
     */
    public void error(String message, Object... args) {
        logger.error("[{}] " + message, prependClassName(args));
    }

    /**
     * Logs an error with exception.
     */
    public void error(String message, Throwable throwable) {
        logger.error("[{}] {} - Exception: {}", className, message, throwable.getMessage(), throwable);
    }

    /**
     * Logs an error for a specific operation.
     */
    public void logError(String operation, String entityType, Throwable ex) {
        logger.error("[{}] Error during {} operation for {}: {}", 
                className, operation, entityType, ex.getMessage(), ex);
    }

    /**
     * Logs an error for a specific operation with entity ID.
     */
    public void logError(String operation, String entityType, Object entityId, Throwable ex) {
        logger.error("[{}] Error during {} operation for {} with id={}: {}", 
                className, operation, entityType, entityId, ex.getMessage(), ex);
    }

    /**
     * Logs an error without exception.
     */
    public void logError(String operation, String entityType, String errorMessage) {
        logger.error("[{}] Error during {} operation for {}: {}", 
                className, operation, entityType, errorMessage);
    }

    /**
     * Logs an error with entity ID without exception.
     */
    public void logError(String operation, String entityType, Object entityId, String errorMessage) {
        logger.error("[{}] Error during {} operation for {} with id={}: {}", 
                className, operation, entityType, entityId, errorMessage);
    }

    // ===================== Timed Execution =====================

    /**
     * Executes a supplier and logs the execution time.
     */
    public <T> T timed(String operation, String entityType, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        logStart(operation, entityType);
        try {
            T result = supplier.get();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[{}] Completed {} operation for {} in {}ms", className, operation, entityType, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed {} operation for {} after {}ms: {}", 
                    className, operation, entityType, duration, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Executes a runnable and logs the execution time.
     */
    public void timed(String operation, String entityType, Runnable runnable) {
        long startTime = System.currentTimeMillis();
        logStart(operation, entityType);
        try {
            runnable.run();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[{}] Completed {} operation for {} in {}ms", className, operation, entityType, duration);
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed {} operation for {} after {}ms: {}", 
                    className, operation, entityType, duration, ex.getMessage(), ex);
            throw ex;
        }
    }

    // ===================== Security Logging =====================

    /**
     * Logs authentication attempt.
     */
    public void logAuthAttempt(String username, boolean success) {
        if (success) {
            logger.info("[{}] Authentication successful for user: {}", className, username);
        } else {
            logger.warn("[{}] Authentication failed for user: {}", className, username);
        }
    }

    /**
     * Logs access denied.
     */
    public void logAccessDenied(String username, String resource) {
        logger.warn("[{}] Access denied for user {} to resource: {}", className, username, resource);
    }

    /**
     * Logs security event.
     */
    public void logSecurityEvent(String event, String details) {
        logger.warn("[{}] Security event - {}: {}", className, event, details);
    }

    // ===================== Business Logging =====================

    /**
     * Logs a business event.
     */
    public void logBusinessEvent(String event, String details) {
        logger.info("[{}] Business event - {}: {}", className, event, details);
    }

    /**
     * Logs a state transition.
     */
    public void logStateTransition(String entityType, Object entityId, String fromState, String toState) {
        logger.info("[{}] State transition for {} id={}: {} -> {}", 
                className, entityType, entityId, fromState, toState);
    }

    // ===================== Helper Methods =====================

    private Object[] prependClassName(Object... args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = className;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return newArgs;
    }
}
