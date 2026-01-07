package com.ffms.resqeats.security;

import java.lang.annotation.*;

/**
 * Annotation to inject the current authenticated user into controller methods.
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
