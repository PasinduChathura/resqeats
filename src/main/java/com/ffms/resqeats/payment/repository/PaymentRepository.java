package com.ffms.resqeats.payment.repository;

import com.ffms.resqeats.payment.entity.Payment;
import com.ffms.resqeats.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Payment repository per SRS Section 6.10.
 */
@Repository
public interface PaymentRepository extends com.ffms.resqeats.common.repository.BaseScopedRepository<Payment> {

    /**
     * Note: Payments are scoped indirectly via their associated Order (order_id).
     * Access checks must be implemented in service layer using OrderRepository.
     */

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByIpgTransactionId(String ipgTransactionId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.status = 'AUTHORIZED' AND p.authorizedAt < :cutoff")
    java.util.List<Payment> findStaleAuthorizations(@Param("cutoff") java.time.LocalDateTime cutoff);

    boolean existsByOrderId(Long orderId);

    /**
     * MEDIUM FIX (Issue #12): Find payment by idempotency key for idempotent operations.
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
