package com.ffms.resqeats.common.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AOP Aspect for centralized logging across the application.
 * Automatically logs method entry, exit, execution time, and exceptions.
 */
@Aspect
@Component
public class LoggingAspect {

    

    // ===================== Service Layer Logging =====================

    /**
     * Pointcut for all service implementations.
     */
    @Pointcut("execution(* com.ffms.resqeats.service..*Impl.*(..))")
    public void serviceLayer() {}

    /**
     * Pointcut for all controller methods.
     */
    @Pointcut("execution(* com.ffms.resqeats.controller..*.*(..))")
    public void controllerLayer() {}

    /**
     * Pointcut for methods annotated with @Loggable.
     */
    @Pointcut("@annotation(com.ffms.resqeats.common.logging.Loggable)")
    public void loggableAnnotation() {}

    /**
     * Pointcut for classes annotated with @Loggable.
     */
    @Pointcut("@within(com.ffms.resqeats.common.logging.Loggable)")
    public void loggableClass() {}

    // ===================== Around Advice =====================

    /**
     * Around advice for service layer methods - logs entry, exit, timing, and exceptions.
     */
    @Around("serviceLayer()")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "Service");
    }

    /**
     * Around advice for controller layer methods.
     */
    @Around("controllerLayer()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "Controller");
    }

    /**
     * Around advice for @Loggable annotated methods.
     */
    @Around("loggableAnnotation()")
    public Object logAnnotatedMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Loggable loggable = method.getAnnotation(Loggable.class);
        
        String operation = loggable.operation().isEmpty() ? method.getName() : loggable.operation();
        String entity = loggable.entity().isEmpty() ? getEntityFromClass(joinPoint) : loggable.entity();
        
        return logMethodExecutionWithAnnotation(joinPoint, operation, entity, loggable);
    }

    // ===================== Exception Logging =====================

    /**
     * After throwing advice for all service layer methods.
     */
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logServiceException(JoinPoint joinPoint, Throwable ex) {
        logException(joinPoint, ex, "Service");
    }

    /**
     * After throwing advice for all controller layer methods.
     */
    @AfterThrowing(pointcut = "controllerLayer()", throwing = "ex")
    public void logControllerException(JoinPoint joinPoint, Throwable ex) {
        logException(joinPoint, ex, "Controller");
    }

    // ===================== Helper Methods =====================

    private Object logMethodExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String params = formatParams(joinPoint.getArgs());
        
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] Entering {}.{}({})", layer, className, methodName, params);
        } else {
            logger.info("[{}] Entering {}.{}", layer, className, methodName);
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 1000) {
                logger.warn("[{}] {}.{} completed in {}ms (SLOW)", layer, className, methodName, executionTime);
            } else {
                logger.info("[{}] {}.{} completed in {}ms", layer, className, methodName, executionTime);
            }
            
            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("[{}] {}.{} failed after {}ms - Error: {}", 
                    layer, className, methodName, executionTime, ex.getMessage());
            throw ex;
        }
    }

    private Object logMethodExecutionWithAnnotation(ProceedingJoinPoint joinPoint, 
            String operation, String entity, Loggable loggable) throws Throwable {
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        
        if (loggable.logParams()) {
            String params = formatParams(joinPoint.getArgs());
            log(logger, loggable.level(), "[{}] Starting {} operation for {} - params: {}", 
                    className, operation, entity, params);
        } else {
            log(logger, loggable.level(), "[{}] Starting {} operation for {}", 
                    className, operation, entity);
        }
        
        long startTime = loggable.timed() ? System.currentTimeMillis() : 0;
        
        try {
            Object result = joinPoint.proceed();
            
            if (loggable.timed()) {
                long executionTime = System.currentTimeMillis() - startTime;
                if (loggable.logResult() && result != null) {
                    log(logger, loggable.level(), "[{}] Completed {} for {} in {}ms - result: {}", 
                            className, operation, entity, executionTime, summarizeResult(result));
                } else {
                    log(logger, loggable.level(), "[{}] Completed {} for {} in {}ms", 
                            className, operation, entity, executionTime);
                }
            }
            
            return result;
        } catch (Throwable ex) {
            if (loggable.timed()) {
                long executionTime = System.currentTimeMillis() - startTime;
                logger.error("[{}] Failed {} for {} after {}ms - Error: {}", 
                        className, operation, entity, executionTime, ex.getMessage());
            } else {
                logger.error("[{}] Failed {} for {} - Error: {}", 
                        className, operation, entity, ex.getMessage());
            }
            throw ex;
        }
    }

    private void logException(JoinPoint joinPoint, Throwable ex, String layer) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        
        // Don't log stack trace for business exceptions
        if (isBusinessException(ex)) {
            logger.warn("[{}] Business error in {}.{}: {}", layer, className, methodName, ex.getMessage());
        } else {
            logger.error("[{}] Exception in {}.{}: {}", layer, className, methodName, ex.getMessage(), ex);
        }
    }

    private boolean isBusinessException(Throwable ex) {
        String packageName = ex.getClass().getPackageName();
        return packageName.startsWith("com.ffms.resqeats.exception");
    }

    private String formatParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .map(this::formatArg)
                .collect(Collectors.joining(", "));
    }

    private String formatArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        // Mask sensitive fields
        String argString = arg.toString();
        if (argString.toLowerCase().contains("password")) {
            return "[MASKED]";
        }
        // Truncate long strings
        if (argString.length() > 100) {
            return argString.substring(0, 100) + "...";
        }
        return argString;
    }

    private String summarizeResult(Object result) {
        if (result == null) {
            return "null";
        }
        String str = result.toString();
        if (str.length() > 200) {
            return str.substring(0, 200) + "...";
        }
        return str;
    }

    private String getEntityFromClass(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        // Extract entity from class name like "ShopServiceImpl" -> "Shop"
        return className.replace("ServiceImpl", "")
                       .replace("Controller", "")
                       .replace("Service", "");
    }

    private void log(Logger logger, Loggable.LogLevel level, String format, Object... args) {
        switch (level) {
            case DEBUG -> logger.debug(format, args);
            case WARN -> logger.warn(format, args);
            default -> logger.info(format, args);
        }
    }
}
