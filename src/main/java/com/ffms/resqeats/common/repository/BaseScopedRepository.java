package com.ffms.resqeats.common.repository;

import com.ffms.resqeats.common.entity.BaseEntity;
import com.ffms.resqeats.exception.security.AccessDeniedException;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base repository with built-in tenant scope enforcement.
 * 
 * ALL tenant-scoped repositories MUST extend this interface.
 * Provides secondary defense against scope bypass via custom/native queries.
 * 
 * PRIMARY scope enforcement is via Hibernate Filters (TenantFilterAspect).
 * This provides additional GUARD layer for explicit scope validation.
 */
@NoRepositoryBean
public interface BaseScopedRepository<T extends BaseEntity> extends JpaRepository<T, UUID> {

    /**
     * Find by ID with scope validation.
     * Automatically validates that the entity belongs to the current user's scope.
     */
    default Optional<T> findByIdScoped(UUID id) {
        Optional<T> entity = findById(id);
        entity.ifPresent(this::validateScope);
        return entity;
    }

    /**
     * Find all with scope validation.
     * Results are already filtered by Hibernate filters.
     */
    default List<T> findAllScoped() {
        return findAll();
    }

    /**
     * Save with scope validation.
     * Ensures entity tenant IDs match current user's scope.
     */
    default T saveScoped(T entity) {
        validateScopeForSave(entity);
        return save(entity);
    }

    /**
     * Delete with scope validation.
     */
    default void deleteScoped(T entity) {
        validateScope(entity);
        delete(entity);
    }

    /**
     * Delete by ID with scope validation.
     */
    default void deleteByIdScoped(UUID id) {
        findByIdScoped(id).ifPresent(this::delete);
    }

    /**
     * Validate that entity is within current user's scope.
     * Override in subclasses for entity-specific validation.
     */
    default void validateScope(T entity) {
        // Default implementation - override for specific entities
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return; // SUPER_ADMIN and ADMIN bypass scope check
        }
        
        // Subclasses should implement specific scope validation
    }

    /**
     * Validate scope for save/update operations.
     */
    default void validateScopeForSave(T entity) {
        validateScope(entity);
    }

    /**
     * Utility method to check merchant scope.
     */
    default void requireMerchantScope(UUID entityMerchantId) {
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return;
        }
        
        if (entityMerchantId == null) {
            throw new AccessDeniedException("Entity has no merchant scope");
        }
        
        UUID userMerchantId = context.getMerchantId();
        if (userMerchantId == null || !userMerchantId.equals(entityMerchantId)) {
            throw new AccessDeniedException("Access denied: merchant scope mismatch");
        }
    }

    /**
     * Utility method to check outlet scope.
     */
    default void requireOutletScope(UUID entityOutletId) {
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return;
        }
        
        if (context.getRole() == UserRole.MERCHANT) {
            // Merchants can access all their outlets - validated by merchant filter
            return;
        }
        
        if (entityOutletId == null) {
            throw new AccessDeniedException("Entity has no outlet scope");
        }
        
        UUID userOutletId = context.getOutletId();
        if (userOutletId == null || !userOutletId.equals(entityOutletId)) {
            throw new AccessDeniedException("Access denied: outlet scope mismatch");
        }
    }

    /**
     * Utility method to check user ownership scope.
     */
    default void requireUserScope(UUID entityUserId) {
        ResqeatsSecurityContext context = SecurityContextHolder.getContext();
        
        if (context.hasGlobalAccess()) {
            return;
        }
        
        if (entityUserId == null) {
            throw new AccessDeniedException("Entity has no user scope");
        }
        
        UUID currentUserId = context.getUserId();
        if (currentUserId == null || !currentUserId.equals(entityUserId)) {
            throw new AccessDeniedException("Access denied: user scope mismatch");
        }
    }
}
