package com.ffms.resqeats.user.repository;

import com.ffms.resqeats.common.repository.BaseScopedRepository;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User repository per SRS Section 6.3.
 * <p>
 * TENANT SCOPED:
 * - SUPER_ADMIN/ADMIN: Full access to users
 * - MERCHANT: Access to users within their merchant
 * - OUTLET_USER: Access to users within their outlet
 * - USER: Access to their own user record
 */
@Repository
public interface UserRepository extends BaseScopedRepository<User>, JpaSpecificationExecutor<User> {

    /**
     * Ensures required associations are fetched for DTO mapping.
     * Without this, accessing {@code user.getMerchant()} / {@code user.getOutlet()} outside the repository
     * transaction can trigger {@link org.hibernate.LazyInitializationException}.
     */
    @EntityGraph(attributePaths = {"merchant", "outlet"})
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = {"merchant", "outlet"})
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Override
    default void validateScope(User entity) {
        if (entity == null) return;
        var context = SecurityContextHolder.getContext();
        if (context.hasGlobalAccess()) return;
        if (context.getRole() == UserRole.MERCHANT_USER) {
            requireMerchantScope(entity.getMerchantId());
            return;
        }
        if (context.hasOutletScope()) {
            requireOutletScope(entity.getOutletId());
            return;
        }
        // Regular users can access only their own user record
        requireUserScope(entity.getId());
    }

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneOrEmail(String phone, String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.oauth2Provider = :provider AND u.oauth2ProviderId = :providerId")
    Optional<User> findByOAuth2ProviderAndProviderId(@Param("provider") String provider,
                                                     @Param("providerId") String providerId);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    List<User> findByMerchantId(Long merchantId);

    List<User> findByOutletId(Long outletId);

    @Query("SELECT u FROM User u WHERE u.merchantId = :merchantId AND u.role = 'OUTLET_USER'")
    List<User> findOutletUsersByMerchantId(@Param("merchantId") Long merchantId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    long countActiveByRole(@Param("role") UserRole role);
}
