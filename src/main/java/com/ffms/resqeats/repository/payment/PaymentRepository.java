package com.ffms.resqeats.repository.payment;

import com.ffms.resqeats.enums.payment.PaymentStatus;
import com.ffms.resqeats.models.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByPreAuthTransactionId(String transactionId);

    Optional<Payment> findByCaptureTransactionId(String transactionId);
}
