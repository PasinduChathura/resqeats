package com.ffms.resqeats.security.rbac;

import com.ffms.resqeats.exception.security.InsufficientRoleException;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect for enforcing @RequireRole annotations.
 * 
 * This provides declarative RBAC at the method level.
 * Executed BEFORE method body.
 */
@Aspect
@Component
@Order(1) // Execute before other aspects
@Slf4j
public class RbacEnforcementAspect {

    @Around("@annotation(com.ffms.resqeats.security.rbac.RequireRole)")
    public Object enforceRoleRequirement(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole annotation = method.getAnnotation(RequireRole.class);
        
        if (annotation != null) {
            UserRole requiredRole = annotation.value();
            ResqeatsSecurityContext context = SecurityContextHolder.getContext();
            
            if (context.isAnonymous()) {
                log.warn("Anonymous user attempted to access @RequireRole protected method: {}", 
                        method.getName());
                throw new InsufficientRoleException("Authentication required");
            }
            
            if (!context.hasRole(requiredRole)) {
                log.warn("User {} with role {} attempted to access method {} requiring role {}",
                        context.getUserId(), context.getRole(), method.getName(), requiredRole);
                throw new InsufficientRoleException(annotation.message());
            }
            
            log.debug("Role check passed for user {} accessing {}", 
                    context.getUserId(), method.getName());
        }
        
        return joinPoint.proceed();
    }

    @Around("@within(com.ffms.resqeats.security.rbac.RequireRole)")
    public Object enforceClassRoleRequirement(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        RequireRole annotation = targetClass.getAnnotation(RequireRole.class);
        
        // Check if method has its own annotation (takes precedence)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(RequireRole.class)) {
            // Method annotation will be handled by the other advice
            return joinPoint.proceed();
        }
        
        if (annotation != null) {
            UserRole requiredRole = annotation.value();
            ResqeatsSecurityContext context = SecurityContextHolder.getContext();
            
            if (context.isAnonymous()) {
                log.warn("Anonymous user attempted to access @RequireRole protected class: {}", 
                        targetClass.getSimpleName());
                throw new InsufficientRoleException("Authentication required");
            }
            
            if (!context.hasRole(requiredRole)) {
                log.warn("User {} with role {} attempted to access class {} requiring role {}",
                        context.getUserId(), context.getRole(), targetClass.getSimpleName(), requiredRole);
                throw new InsufficientRoleException(annotation.message());
            }
        }
        
        return joinPoint.proceed();
    }
}
