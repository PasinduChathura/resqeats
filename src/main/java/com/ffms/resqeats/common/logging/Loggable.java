package com.ffms.resqeats.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable automatic logging for methods.
 * Can be applied at class or method level.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {

    /**
     * The operation name for logging.
     */
    String operation() default "";

    /**
     * The entity type being operated on.
     */
    String entity() default "";

    /**
     * Whether to log method parameters.
     */
    boolean logParams() default true;

    /**
     * Whether to log the return value.
     */
    boolean logResult() default false;

    /**
     * Whether to log execution time.
     */
    boolean timed() default true;

    /**
     * Log level: INFO, DEBUG, WARN
     */
    LogLevel level() default LogLevel.INFO;

    enum LogLevel {
        DEBUG, INFO, WARN
    }
}
