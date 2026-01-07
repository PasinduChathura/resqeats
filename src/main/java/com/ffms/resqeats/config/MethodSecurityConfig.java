package com.ffms.resqeats.config;

import com.ffms.resqeats.security.CustomMethodSecurityExpressionHandler;
import com.ffms.resqeats.security.CustomPermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    @Bean
    protected MethodSecurityExpressionHandler createExpressionHandler(RoleHierarchy roleHierarchy) {
        final CustomMethodSecurityExpressionHandler expressionHandler = new CustomMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new CustomPermissionEvaluator());
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }
}