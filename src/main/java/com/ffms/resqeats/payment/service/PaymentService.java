package com.ffms.resqeats.payment.service;

import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.order.entity.Order;
import com.ffms.resqeats.payment.entity.Payment;
import com.ffms.resqeats.payment.entity.PaymentMethod;
import com.ffms.resqeats.payment.enums.PaymentStatus;
import com.ffms.resqeats.payment.repository.PaymentMethodRepository;
import com.ffms.resqeats.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing payment operations including pre-authorization, capture, void, and refund.
 *
 * <p>Implements payment processing per SRS Section 6.10 with the following flow:</p>
 * <ul>
 *   <li>Card Registration: Token stored via IPG integration</li>
 *   <li>Checkout: Pre-authorization to hold funds</li>
 *   <li>Outlet Accept: Capture to transfer funds</li>
 *   <li>Outlet Decline/Timeout: Void to release held funds</li>
 * </ul>
 *
 * <p>Business Rules enforced:</p>
 * <ul>
 *   <li>BR-001: Orders cannot be placed without successful pre-authorization</li>
 *   <li>BR-004: Payment captured only upon outlet acceptance</li>
 *   <li>BR-005: Pre-authorization voided if outlet declines or times out</li>
 *   <li>BR-006: Cash-on-delivery is NOT supported (online payment only)</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    /**
     * Pre-authorizes payment by holding funds without capture.
     *
     * <p>Per BR-001, this method must be called successfully before an order can be placed.
     * Implements idempotency using a unique key per order to handle retry scenarios safely.
     * Validates payment method ownership to ensure security.</p>
     *
     * @param order the order requiring payment authorization
     * @param paymentMethodId the UUID of the payment method to use
     * @return the authorized Payment entity
     * @throws BusinessException with code PAY_001 when payment already exists or authorization fails
     * @throws BusinessException with code PAY_003 when payment method is invalid, expired, inactive, or not owned by user
     */
    @Transactional
    public Payment preAuthorize(Order order, UUID paymentMethodId) {
        log.info("Pre-authorizing payment for order: {}, paymentMethodId: {}", order.getId(), paymentMethodId);
        
        String idempotencyKey = "preauth:" + order.getId();
        log.debug("Generated idempotency key: {}", idempotencyKey);
        
        Payment existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existingPayment != null) {
            log.info("Returning existing payment for idempotency key: {}, paymentId: {}", idempotencyKey, existingPayment.getId());
            return existingPayment;
        }
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> {
                    log.warn("Payment method not found: {}", paymentMethodId);
                    return new BusinessException("PAY_003", "Invalid payment method");
                });

        if (!paymentMethod.getUserId().equals(order.getUserId())) {
            log.warn("Payment method ownership mismatch: method user {} != order user {}", 
                    paymentMethod.getUserId(), order.getUserId());
            throw new BusinessException("PAY_003", "Payment method does not belong to this user");
        }

        if (paymentMethod.isExpired()) {
            log.warn("Payment method expired: {}", paymentMethodId);
            throw new BusinessException("PAY_003", "Payment method has expired");
        }

        if (!Boolean.TRUE.equals(paymentMethod.getIsActive())) {
            log.warn("Payment method not active: {}", paymentMethodId);
            throw new BusinessException("PAY_003", "Payment method is not active");
        }

        if (paymentRepository.existsByOrderId(order.getId())) {
            log.warn("Payment already exists for order: {}", order.getId());
            throw new BusinessException("PAY_001", "Payment already exists for this order");
        }

        log.debug("Creating payment record for order: {}, amount: {}", order.getId(), order.getTotal());
        Payment payment = Payment.builder()
                .orderId(order.getId())
                .amount(order.getTotal())
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .paymentMethodId(paymentMethodId)
                .paymentMethodToken(paymentMethod.getToken())
                .idempotencyKey(idempotencyKey)
                .build();

        payment = paymentRepository.save(payment);
        log.debug("Payment record created with id: {}", payment.getId());

        try {
            String authorizationCode = simulatePreAuthorization(payment);

            payment.setStatus(PaymentStatus.AUTHORIZED);
            payment.setAuthorizationCode(authorizationCode);
            payment.setIpgTransactionId("IPG-" + UUID.randomUUID().toString().substring(0, 8));
            payment.setAuthorizedAt(LocalDateTime.now());

            log.info("Payment pre-authorized successfully: paymentId={}, orderId={}, orderNumber={}, amount={}", 
                    payment.getId(), order.getId(), order.getOrderNumber(), order.getTotal());
            return paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Payment authorization failed for order: {}, error: {}", order.getId(), e.getMessage(), e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            throw new BusinessException("PAY_001", "Payment authorization failed: " + e.getMessage());
        }
    }

    /**
     * Captures a pre-authorized payment to transfer funds.
     *
     * <p>Per BR-004, this method is called only upon outlet acceptance of the order.
     * The payment must be in AUTHORIZED status to be captured.</p>
     *
     * @param orderId the UUID of the order whose payment should be captured
     * @return the captured Payment entity
     * @throws BusinessException with code PAY_002 when payment not found, cannot be captured, or capture fails
     */
    @Transactional
    public Payment capturePayment(UUID orderId) {
        log.info("Capturing payment for order: {}", orderId);
        
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment not found for order: {}", orderId);
                    return new BusinessException("PAY_002", "Payment not found for order");
                });

        if (!payment.canBeCaptured()) {
            log.warn("Payment cannot be captured: paymentId={}, currentStatus={}", payment.getId(), payment.getStatus());
            throw new BusinessException("PAY_002", 
                    "Payment cannot be captured. Current status: " + payment.getStatus());
        }

        try {
            log.debug("Calling IPG for capture: paymentId={}, amount={}", payment.getId(), payment.getAmount());
            String captureCode = simulateCapture(payment);

            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setCaptureCode(captureCode);
            payment.setCapturedAt(LocalDateTime.now());

            log.info("Payment captured successfully: paymentId={}, orderId={}, captureCode={}", 
                    payment.getId(), orderId, captureCode);
            return paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Payment capture failed for order: {}, paymentId={}, error: {}", 
                    orderId, payment.getId(), e.getMessage(), e);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            throw new BusinessException("PAY_002", "Payment capture failed: " + e.getMessage());
        }
    }

    /**
     * Voids a pre-authorization to release held funds.
     *
     * <p>Per BR-005, this method is called when the outlet declines the order or when
     * the order times out. If the payment cannot be voided (already in terminal state),
     * the method returns the existing payment without throwing an exception.</p>
     *
     * @param orderId the UUID of the order whose payment should be voided
     * @return the voided Payment entity or existing payment if already in terminal state
     * @throws BusinessException with code PAY_002 when payment not found or void operation fails
     */
    @Transactional
    public Payment voidPreAuthorization(UUID orderId) {
        log.info("Voiding pre-authorization for order: {}", orderId);
        
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment not found for order: {}", orderId);
                    return new BusinessException("PAY_002", "Payment not found for order");
                });

        if (!payment.canBeVoided()) {
            log.warn("Payment cannot be voided: paymentId={}, currentStatus={}", payment.getId(), payment.getStatus());
            return payment;
        }

        try {
            log.debug("Calling IPG for void: paymentId={}", payment.getId());
            simulateVoid(payment);

            payment.setStatus(PaymentStatus.VOIDED);
            payment.setVoidedAt(LocalDateTime.now());

            log.info("Payment voided successfully: paymentId={}, orderId={}", payment.getId(), orderId);
            return paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Payment void failed for order: {}, paymentId={}, error: {}", 
                    orderId, payment.getId(), e.getMessage(), e);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            throw new BusinessException("PAY_002", "Payment void failed: " + e.getMessage());
        }
    }

    /**
     * Processes a refund for a previously captured payment.
     *
     * <p>Only payments in CAPTURED status can be refunded. The refund reason is recorded
     * for audit purposes.</p>
     *
     * @param orderId the UUID of the order whose payment should be refunded
     * @param reason the reason for the refund
     * @return the refunded Payment entity
     * @throws BusinessException with code PAY_002 when payment not found, cannot be refunded, or refund fails
     */
    @Transactional
    public Payment refundPayment(UUID orderId, String reason) {
        log.info("Processing refund for order: {}, reason: {}", orderId, reason);
        
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment not found for order: {}", orderId);
                    return new BusinessException("PAY_002", "Payment not found for order");
                });

        if (!payment.canBeRefunded()) {
            log.warn("Payment cannot be refunded: paymentId={}, currentStatus={}", payment.getId(), payment.getStatus());
            throw new BusinessException("PAY_002", 
                    "Payment cannot be refunded. Current status: " + payment.getStatus());
        }

        try {
            log.debug("Calling IPG for refund: paymentId={}, amount={}", payment.getId(), payment.getAmount());
            String refundTransactionId = simulateRefund(payment);

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundTransactionId(refundTransactionId);
            payment.setRefundedAt(LocalDateTime.now());

            log.info("Payment refunded successfully: paymentId={}, orderId={}, refundTransactionId={}", 
                    payment.getId(), orderId, refundTransactionId);
            return paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Payment refund failed for order: {}, paymentId={}, error: {}", 
                    orderId, payment.getId(), e.getMessage(), e);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            throw new BusinessException("PAY_002", "Payment refund failed: " + e.getMessage());
        }
    }

    /**
     * Handles incoming IPG webhook notifications.
     *
     * <p>This method is idempotent - if the payment is already in a terminal state
     * (CAPTURED, REFUNDED, or VOIDED), the webhook is acknowledged but ignored.
     * Unknown transaction IDs are logged as warnings but do not cause errors.</p>
     *
     * @param ipgTransactionId the IPG transaction identifier
     * @param status the status from the webhook payload
     * @param payload the raw webhook payload for auditing
     */
    @Transactional
    public void handleWebhook(String ipgTransactionId, String status, String payload) {
        log.info("Handling IPG webhook: transactionId={}, status={}", ipgTransactionId, status);
        log.debug("Webhook payload: {}", payload);
        
        Payment payment = paymentRepository.findByIpgTransactionId(ipgTransactionId)
                .orElse(null);

        if (payment == null) {
            log.warn("Webhook received for unknown transaction: {}", ipgTransactionId);
            return;
        }

        if (payment.getStatus() == PaymentStatus.CAPTURED || 
            payment.getStatus() == PaymentStatus.REFUNDED ||
            payment.getStatus() == PaymentStatus.VOIDED) {
            log.info("Ignoring webhook for payment in terminal state: paymentId={}, status={}", 
                    payment.getId(), payment.getStatus());
            return;
        }

        payment.setGatewayResponse(payload);
        paymentRepository.save(payment);

        log.info("Webhook processed successfully: paymentId={}, transactionId={}", payment.getId(), ipgTransactionId);
    }

    /**
     * Retrieves a payment by its associated order ID.
     *
     * @param orderId the UUID of the order
     * @return the Payment entity associated with the order
     * @throws BusinessException with code PAY_002 when payment not found for the order
     */
    public Payment getPaymentByOrderId(UUID orderId) {
        log.info("Fetching payment for orderId: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment not found for order: {}", orderId);
                    return new BusinessException("PAY_002", "Payment not found for order");
                });
        log.debug("Found payment: paymentId={}, status={}, amount={}", payment.getId(), payment.getStatus(), payment.getAmount());
        return payment;
    }

    /**
     * Adds a new payment method for a user by tokenizing the card via IPG.
     *
     * <p>If this is the user's first payment method, it is automatically set as default.
     * If the request specifies setAsDefault, all other payment methods are updated to non-default.</p>
     *
     * @param userId the UUID of the user
     * @param request the payment method details including card information
     * @return the created PaymentMethod entity
     */
    @Transactional
    public PaymentMethod addPaymentMethod(UUID userId, AddPaymentMethodRequest request) {
        log.info("Adding payment method for user: {}, cardBrand: {}", userId, detectCardBrand(request.getCardNumber()));
        
        String token = "tok_" + UUID.randomUUID().toString();
        log.debug("Generated token for payment method");

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .userId(userId)
                .token(token)
                .cardLastFour(request.getCardNumber().substring(request.getCardNumber().length() - 4))
                .cardBrand(detectCardBrand(request.getCardNumber()))
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .cardholderName(request.getCardholderName())
                .isDefault(request.isSetAsDefault())
                .build();

        if (request.isSetAsDefault()) {
            log.debug("Clearing existing default payment method for user: {}", userId);
            paymentMethodRepository.clearDefaultForUser(userId);
        }

        if (paymentMethodRepository.countActiveByUserId(userId) == 0) {
            log.debug("First payment method for user, setting as default: {}", userId);
            paymentMethod.setIsDefault(true);
        }

        PaymentMethod savedMethod = paymentMethodRepository.save(paymentMethod);
        log.info("Payment method added successfully: paymentMethodId={}, userId={}, last4={}", 
                savedMethod.getId(), userId, savedMethod.getCardLastFour());
        return savedMethod;
    }

    /**
     * Retrieves all active payment methods for a user.
     *
     * @param userId the UUID of the user
     * @return list of active PaymentMethod entities for the user
     */
    public List<PaymentMethod> getUserPaymentMethods(UUID userId) {
        log.info("Fetching payment methods for user: {}", userId);
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findByUserIdAndIsActiveTrue(userId);
        log.debug("Found {} active payment methods for user: {}", paymentMethods.size(), userId);
        return paymentMethods;
    }

    /**
     * Retrieves the default payment method for a user.
     *
     * @param userId the UUID of the user
     * @return the default PaymentMethod entity, or null if no default is set
     */
    public PaymentMethod getDefaultPaymentMethod(UUID userId) {
        log.info("Fetching default payment method for user: {}", userId);
        PaymentMethod paymentMethod = paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElse(null);
        if (paymentMethod != null) {
            log.debug("Default payment method found: paymentMethodId={}, last4={}", 
                    paymentMethod.getId(), paymentMethod.getCardLastFour());
        } else {
            log.debug("No default payment method found for user: {}", userId);
        }
        return paymentMethod;
    }

    /**
     * Removes a payment method by marking it as inactive.
     *
     * <p>The payment method is soft-deleted by setting isActive to false.
     * Only the owner of the payment method can remove it.</p>
     *
     * @param paymentMethodId the UUID of the payment method to remove
     * @param userId the UUID of the user requesting the removal
     * @throws BusinessException with code PAY_003 when payment method not found
     * @throws BusinessException with code AUTH_003 when user is not the owner
     */
    @Transactional
    public void removePaymentMethod(UUID paymentMethodId, UUID userId) {
        log.info("Removing payment method: paymentMethodId={}, userId={}", paymentMethodId, userId);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> {
                    log.warn("Payment method not found: {}", paymentMethodId);
                    return new BusinessException("PAY_003", "Payment method not found");
                });

        if (!paymentMethod.getUserId().equals(userId)) {
            log.warn("Unauthorized removal attempt: paymentMethodId={}, ownerUserId={}, requestUserId={}", 
                    paymentMethodId, paymentMethod.getUserId(), userId);
            throw new BusinessException("AUTH_003", "Not authorized to delete this payment method");
        }

        paymentMethod.setIsActive(false);
        paymentMethodRepository.save(paymentMethod);

        log.info("Payment method removed successfully: paymentMethodId={}, userId={}", paymentMethodId, userId);
    }

    /**
     * Sets a payment method as the default for a user.
     *
     * <p>Clears the default flag from all other payment methods before setting
     * the specified one as default. Only the owner can set a payment method as default.</p>
     *
     * @param paymentMethodId the UUID of the payment method to set as default
     * @param userId the UUID of the user
     * @throws BusinessException with code PAY_003 when payment method not found
     * @throws BusinessException with code AUTH_003 when user is not the owner
     */
    @Transactional
    public void setDefaultPaymentMethod(UUID paymentMethodId, UUID userId) {
        log.info("Setting default payment method: paymentMethodId={}, userId={}", paymentMethodId, userId);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> {
                    log.warn("Payment method not found: {}", paymentMethodId);
                    return new BusinessException("PAY_003", "Payment method not found");
                });

        if (!paymentMethod.getUserId().equals(userId)) {
            log.warn("Unauthorized default setting attempt: paymentMethodId={}, ownerUserId={}, requestUserId={}", 
                    paymentMethodId, paymentMethod.getUserId(), userId);
            throw new BusinessException("AUTH_003", "Not authorized");
        }

        log.debug("Clearing existing default payment methods for user: {}", userId);
        paymentMethodRepository.clearDefaultForUser(userId);
        paymentMethod.setIsDefault(true);
        paymentMethodRepository.save(paymentMethod);
        
        log.info("Default payment method set successfully: paymentMethodId={}, userId={}", paymentMethodId, userId);
    }

    /**
     * Simulates IPG pre-authorization for testing purposes.
     *
     * <p>This method should be replaced with actual payment gateway integration.</p>
     *
     * @param payment the payment to pre-authorize
     * @return the simulated authorization code
     */
    private String simulatePreAuthorization(Payment payment) {
        log.debug("Simulating pre-authorization for payment: {}", payment.getId());
        return "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Simulates IPG capture for testing purposes.
     *
     * <p>This method should be replaced with actual payment gateway integration.</p>
     *
     * @param payment the payment to capture
     * @return the simulated capture code
     */
    private String simulateCapture(Payment payment) {
        log.debug("Simulating capture for payment: {}", payment.getId());
        return "CAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Simulates IPG void for testing purposes.
     *
     * <p>This method should be replaced with actual payment gateway integration.</p>
     *
     * @param payment the payment to void
     */
    private void simulateVoid(Payment payment) {
        log.debug("Simulating void for payment: {}", payment.getId());
    }

    /**
     * Simulates IPG refund for testing purposes.
     *
     * <p>This method should be replaced with actual payment gateway integration.</p>
     *
     * @param payment the payment to refund
     * @return the simulated refund transaction ID
     */
    private String simulateRefund(Payment payment) {
        log.debug("Simulating refund for payment: {}", payment.getId());
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Detects the card brand based on the card number prefix.
     *
     * @param cardNumber the card number to analyze
     * @return the detected card brand (Visa, Mastercard, Amex, or Unknown)
     */
    private String detectCardBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) return "Visa";
        if (cardNumber.startsWith("5")) return "Mastercard";
        if (cardNumber.startsWith("37")) return "Amex";
        return "Unknown";
    }

    /**
     * Request DTO for adding a new payment method.
     *
     * <p>Contains all necessary card information for tokenization via IPG.
     * The CVV is used only for initial verification and is not stored.</p>
     *
     * @author ResqEats Team
     * @version 1.0
     * @since 2024-01-01
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AddPaymentMethodRequest {
        /** The full card number for tokenization. */
        private String cardNumber;
        /** The card expiry month (1-12). */
        private Integer expiryMonth;
        /** The card expiry year (4-digit). */
        private Integer expiryYear;
        /** The card CVV/CVC for verification (not stored). */
        private String cvv;
        /** The name on the card. */
        private String cardholderName;
        /** Whether to set this as the default payment method. */
        private boolean setAsDefault;
    }
}
