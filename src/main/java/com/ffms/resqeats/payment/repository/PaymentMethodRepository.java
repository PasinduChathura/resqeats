package com.ffms.resqeats.payment.repository;

import com.ffms.resqeats.payment.entity.PaymentMethod;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PaymentMethod repository.
 */
@Repository
public interface PaymentMethodRepository extends com.ffms.resqeats.common.repository.BaseScopedRepository<PaymentMethod> {

    @Override
    default void validateScope(PaymentMethod entity) {
        if (entity == null) return;
        // Payment methods are user-scoped
        requireUserScope(entity.getUserId());
    }

    List<PaymentMethod> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(UUID userId);

    Optional<PaymentMethod> findByToken(String token);

    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.userId = :userId")
    void clearDefaultForUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(pm) FROM PaymentMethod pm WHERE pm.userId = :userId AND pm.isActive = true")
    long countActiveByUserId(@Param("userId") UUID userId);

    boolean existsByUserIdAndToken(UUID userId, String token);
}
