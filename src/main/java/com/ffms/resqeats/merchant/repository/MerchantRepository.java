package com.ffms.resqeats.merchant.repository;

import com.ffms.resqeats.common.repository.BaseScopedRepository;
import com.ffms.resqeats.merchant.entity.Merchant;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Merchant repository per SRS Section 6.4.
 * <p>
 * TENANT SCOPE: Merchants are global resources managed by ADMIN/SUPER_ADMIN.
 * MERCHANT_USER users are assigned to merchants via User.merchantId.
 * <p>
 * Note: Uses JpaSpecificationExecutor for dynamic filtering via MerchantSpecification.
 */
@Repository
public interface MerchantRepository extends BaseScopedRepository<Merchant>, JpaSpecificationExecutor<Merchant> {

    // ============== STATUS-BASED QUERIES ==============

    /**
     * Find merchants by status with pagination.
     * Used for public merchant listing (APPROVED only).
     */
    Page<Merchant> findByStatus(MerchantStatus status, Pageable pageable);

    // ============== SEARCH METHODS ==============

    /**
     * Search merchants by name (case-insensitive).
     * Used for admin lookup functionality.
     */
    List<Merchant> findByNameContainingIgnoreCase(String name);

    /**
     * Search merchants by name with pagination.
     * Used for public merchant search.
     */
    Page<Merchant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // ============== VALIDATION METHODS ==============

    /**
     * Check if registration number already exists.
     */
    boolean existsByRegistrationNo(String registrationNo);

    // ============== SCOPE VALIDATION ==============

    /**
     * Validates merchant scope based on current user context.
     * ADMIN/SUPER_ADMIN: Full access
     * MERCHANT_USER: Access only to their assigned merchant
     */
    @Override
    default void validateScope(Merchant entity) {
        if (entity == null) return;
        var context = SecurityContextHolder.getContext();
        // ADMIN and SUPER_ADMIN bypass
        if (context.hasGlobalAccess()) return;
        // MERCHANT_USER can access their assigned merchant
        if (context.getMerchantId() != null && context.getMerchantId().equals(entity.getId())) return;
        throw new com.ffms.resqeats.exception.security.AccessDeniedException("Access denied: Merchant scope mismatch");
    }
}
