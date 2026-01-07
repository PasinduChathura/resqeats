package com.ffms.resqeats.security.rbac;

import com.ffms.resqeats.user.enums.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require a minimum role for method access.
 * 
 * Usage:
 * @RequireRole(UserRole.ADMIN)
 * public void adminOnlyMethod() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * Minimum role required to access this method.
     */
    UserRole value();
    
    /**
     * Optional message for access denied error.
     */
    String message() default "Insufficient permissions";
}
