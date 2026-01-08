package com.ffms.resqeats.merchant.repository;

import com.ffms.resqeats.common.repository.BaseScopedRepository;
import com.ffms.resqeats.merchant.entity.Merchant;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Merchant repository per SRS Section 6.4.
 * 
 * TENANT SCOPE: Merchants are global resources managed by ADMIN/SUPER_ADMIN,
 * but merchant owners (users with ownerUserId) can manage their own merchant.
 */
@Repository
public interface MerchantRepository extends BaseScopedRepository<Merchant>, JpaSpecificationExecutor<Merchant> {

    Optional<Merchant> findByOwnerUserId(UUID ownerUserId);

    boolean existsByOwnerUserId(UUID ownerUserId);

    Page<Merchant> findByStatus(MerchantStatus status, Pageable pageable);

    List<Merchant> findByStatus(MerchantStatus status);

    Page<Merchant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT m FROM Merchant m WHERE m.name LIKE %:query% OR m.legalName LIKE %:query%")
    Page<Merchant> searchByName(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Merchant m WHERE m.status = 'APPROVED'")
    long countApprovedMerchants();

    @Query("SELECT COUNT(m) FROM Merchant m WHERE m.status = 'PENDING'")
    long countPendingMerchants();

    boolean existsByRegistrationNo(String registrationNo);

    boolean existsByContactEmail(String contactEmail);

    @Override
    default void validateScope(Merchant entity) {
        if (entity == null) return;
        var context = SecurityContextHolder.getContext();
        // ADMIN and SUPER_ADMIN bypass
        if (context.hasGlobalAccess()) return;
        // Owner user can manage their merchant
        if (entity.getOwnerUserId() != null && entity.getOwnerUserId().equals(context.getUserId())) return;
        throw new com.ffms.resqeats.exception.security.AccessDeniedException("Access denied: Merchant scope mismatch");
    }
}
