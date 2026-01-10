package com.ffms.resqeats.security.audit;

import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Centralized Audit Service for logging security-sensitive operations.
 *
 * <p>This service provides a consistent and comprehensive way to log security events
 * across the application. All SUPER_ADMIN and ADMIN actions are audited as required
 * by the SRS security requirements.</p>
 *
 * <p>Audit events are categorized by type:</p>
 * <ul>
 *   <li>Admin access events - privileged resource access</li>
 *   <li>Data modification events - create, update, delete operations</li>
 *   <li>Authentication events - login, logout, token operations</li>
 *   <li>Authorization failures - access denied scenarios</li>
 *   <li>Cross-tenant attempts - multi-tenancy violation attempts</li>
 *   <li>Sensitive data access - PII and confidential data access</li>
 *   <li>Role changes - privilege escalation and demotion</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class AuditService {

    private static final String AUDIT_PREFIX = "SECURITY_AUDIT";

    /**
     * Logs an administrative access event for privileged operations.
     *
     * <p>This method records when administrators access protected resources.
     * Logging is conditional based on the user's role requiring audit trail.</p>
     *
     * @param resourceType the type of resource being accessed (e.g., "USER", "MERCHANT")
     * @param resourceId the unique identifier of the resource
     * @param action the action being performed (e.g., "VIEW", "UPDATE", "DELETE")
     */
    public void logAdminAccess(String resourceType, Long resourceId, String action) {
        log.debug("Checking audit requirement for admin access: {} on {} ({})",
                action, resourceType, resourceId);
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        if (context.requiresAudit()) {
            log.info("{} | Action: {} | User: {} | Role: {} | Resource: {} | ResourceId: {} | Time: {}",
                    AUDIT_PREFIX, action, context.getUserId(), context.getRole(),
                    resourceType, resourceId, LocalDateTime.now());
        } else {
            log.debug("Audit not required for user {} with role {}",
                    context.getUserId(), context.getRole());
        }
    }

    /**
     * Logs a data modification event for entity changes.
     *
     * <p>This method records all data modification operations including
     * create, update, and delete actions on entities.</p>
     *
     * @param entityType the type of entity being modified (e.g., "ORDER", "ITEM")
     * @param entityId the unique identifier of the entity
     * @param action the modification action (e.g., "CREATE", "UPDATE", "DELETE")
     * @param details additional details about the modification
     */
    public void logDataModification(String entityType, Long entityId, String action, String details) {
        log.debug("Recording data modification: {} on {} ({})", action, entityType, entityId);
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        log.info("{} | Action: {} | User: {} | Role: {} | Entity: {} | EntityId: {} | Details: {} | Time: {}",
                AUDIT_PREFIX, action, context.getUserId(), context.getRole(),
                entityType, entityId, details, LocalDateTime.now());
    }

    /**
     * Logs an authentication event for login and session management.
     *
     * <p>This method records authentication-related events such as successful
     * logins, failed login attempts, and logout operations.</p>
     *
     * @param event the authentication event type (e.g., "LOGIN", "LOGOUT", "LOGIN_FAILED")
     * @param identifier the user identifier (email, phone, or user ID)
     * @param success whether the authentication attempt was successful
     * @param details additional context about the authentication event
     */
    public void logAuthenticationEvent(String event, String identifier, boolean success, String details) {
        log.debug("Recording authentication event: {} for identifier: {}", event, identifier);
        if (success) {
            log.info("{} | Event: {} | Identifier: {} | Success: {} | Details: {} | Time: {}",
                    AUDIT_PREFIX, event, identifier, success, details, LocalDateTime.now());
        } else {
            log.warn("{} | Event: {} | Identifier: {} | Success: {} | Details: {} | Time: {}",
                    AUDIT_PREFIX, event, identifier, success, details, LocalDateTime.now());
        }
    }

    /**
     * Logs an authorization failure when access is denied.
     *
     * <p>This method records when a user attempts to access a resource
     * without the required permissions. These events are logged at WARN
     * level for security monitoring.</p>
     *
     * @param resource the resource that was denied access to
     * @param requiredRole the role required to access the resource
     * @param details additional context about the authorization failure
     */
    public void logAuthorizationFailure(String resource, String requiredRole, String details) {
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        log.debug("Recording authorization failure for user {} attempting to access {}",
                context.getUserId(), resource);
        log.warn("{} | Event: AUTHORIZATION_FAILURE | User: {} | Role: {} | Resource: {} | Required: {} | Details: {} | Time: {}",
                AUDIT_PREFIX, context.getUserId(), context.getRole(), resource,
                requiredRole, details, LocalDateTime.now());
    }

    /**
     * Logs a cross-tenant access attempt for multi-tenancy violations.
     *
     * <p>This method records when a user attempts to access data belonging
     * to a different tenant. These are critical security events logged at
     * ERROR level and may indicate malicious activity.</p>
     *
     * @param entityType the type of entity involved in the cross-tenant attempt
     * @param entityId the unique identifier of the entity
     * @param targetTenantId the tenant ID that was attempted to be accessed
     */
    public void logCrossTenantAttempt(String entityType, Long entityId, Long targetTenantId) {
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        log.debug("CRITICAL: Cross-tenant access attempt detected for user {}", context.getUserId());
        log.error("{} | Event: CROSS_TENANT_ATTEMPT | User: {} | Role: {} | MerchantId: {} | Entity: {} | EntityId: {} | TargetTenant: {} | Time: {}",
                AUDIT_PREFIX, context.getUserId(), context.getRole(), context.getMerchantId(),
                entityType, entityId, targetTenantId, LocalDateTime.now());
    }

    /**
     * Logs access to sensitive data such as PII.
     *
     * <p>This method records when sensitive or personally identifiable
     * information is accessed, including the reason for access to ensure
     * compliance with data protection requirements.</p>
     *
     * @param dataType the type of sensitive data being accessed (e.g., "PII", "PAYMENT_INFO")
     * @param subjectId the unique identifier of the data subject
     * @param reason the business reason for accessing the sensitive data
     */
    public void logSensitiveDataAccess(String dataType, Long subjectId, String reason) {
        log.debug("Recording sensitive data access: {} for subject {}", dataType, subjectId);
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        log.info("{} | Event: SENSITIVE_DATA_ACCESS | User: {} | Role: {} | DataType: {} | SubjectId: {} | Reason: {} | Time: {}",
                AUDIT_PREFIX, context.getUserId(), context.getRole(),
                dataType, subjectId, reason, LocalDateTime.now());
    }

    /**
     * Logs a role change event for privilege modifications.
     *
     * <p>This method records when a user's role is changed, which is
     * critical for security auditing of privilege escalation or demotion.</p>
     *
     * @param targetUserId the unique identifier of the user whose role is being changed
     * @param oldRole the previous role of the user
     * @param newRole the new role being assigned to the user
     */
    public void logRoleChange(Long targetUserId, String oldRole, String newRole) {
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        log.debug("Recording role change for user {} by actor {}", targetUserId, context.getUserId());
        log.info("{} | Event: ROLE_CHANGE | Actor: {} | ActorRole: {} | TargetUser: {} | OldRole: {} | NewRole: {} | Time: {}",
                AUDIT_PREFIX, context.getUserId(), context.getRole(),
                targetUserId, oldRole, newRole, LocalDateTime.now());
    }

    /**
     * Logs a token lifecycle event for JWT management.
     *
     * <p>This method records token-related events such as generation,
     * refresh, and revocation for security monitoring and debugging.</p>
     *
     * @param event the token event type (e.g., "TOKEN_GENERATED", "TOKEN_REFRESHED", "TOKEN_REVOKED")
     * @param userId the unique identifier of the user associated with the token
     * @param tokenId the unique identifier or fingerprint of the token
     */
    public void logTokenEvent(String event, Long userId, String tokenId) {
        log.debug("Recording token event: {} for user {}", event, userId);
        log.info("{} | Event: {} | UserId: {} | TokenId: {} | Time: {}",
                AUDIT_PREFIX, event, userId, tokenId, LocalDateTime.now());
    }
}
