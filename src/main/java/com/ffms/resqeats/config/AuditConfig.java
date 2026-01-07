package com.ffms.resqeats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing configuration.
 * 
 * MEDIUM FIX (Issue #9): Implements AuditorAware to properly populate
 * createdBy/updatedBy fields from SecurityContext instead of hardcoded "system".
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SecurityAuditorAware();
    }

    /**
     * AuditorAware implementation that extracts the current user from SecurityContext.
     */
    static class SecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            String principal = authentication.getName();
            
            // Handle anonymous user
            if ("anonymousUser".equals(principal)) {
                return Optional.of("system");
            }

            return Optional.of(principal);
        }
    }
}
