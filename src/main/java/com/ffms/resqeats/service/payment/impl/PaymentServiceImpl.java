package com.ffms.resqeats.service.payment.impl;

import com.ffms.resqeats.common.logging.AppLogger;
import com.ffms.resqeats.dto.payment.AddPaymentMethodRequest;
import com.ffms.resqeats.dto.payment.PaymentMethodResponse;
import com.ffms.resqeats.dto.payment.PaymentResponse;
import com.ffms.resqeats.dto.payment.PaymentWebhookRequest;
import com.ffms.resqeats.enums.payment.PaymentStatus;
import com.ffms.resqeats.exception.payment.PaymentException;
import com.ffms.resqeats.models.order.Order;
import com.ffms.resqeats.models.payment.Payment;
import com.ffms.resqeats.models.payment.UserPaymentMethod;
import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.repository.payment.PaymentRepository;
import com.ffms.resqeats.repository.payment.UserPaymentMethodRepository;
import com.ffms.resqeats.repository.usermgt.UserRepository;
import com.ffms.resqeats.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final AppLogger appLogger = AppLogger.of(log);

    private final PaymentRepository paymentRepository;
    private final UserPaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    
    @Value("${payment.webhook.secret:}")
    private String webhookSecret;

    // ===================== Payment Method Management =====================

    @Override
    @Transactional
    public PaymentMethodResponse addPaymentMethod(Long userId, AddPaymentMethodRequest request) {
        appLogger.logStart("ADD", "PaymentMethod", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    appLogger.logError("ADD", "PaymentMethod", userId, "User not found");
                    return new PaymentException("User not found. Please login again.", "USER_NOT_FOUND");
                });

        if (request.getIsDefault() != null && request.getIsDefault()) {
            // Clear existing default
            List<UserPaymentMethod> existingMethods = paymentMethodRepository.findByUserIdAndIsActiveTrue(userId);
            existingMethods.forEach(pm -> pm.setIsDefault(false));
            paymentMethodRepository.saveAll(existingMethods);
        }

        UserPaymentMethod paymentMethod = UserPaymentMethod.builder()
                .user(user)
                .paymentMethod(request.getPaymentMethod())
                .cardToken(request.getCardToken())
                .cardLastFour(request.getCardLastFour())
                .cardBrand(request.getCardBrand())
                .cardExpiryMonth(request.getCardExpiryMonth())
                .cardExpiryYear(request.getCardExpiryYear())
                .cardHolderName(request.getCardHolderName())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .nickname(request.getNickname())
                .isActive(true)
                .build();

        UserPaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        appLogger.logSuccess("ADD", "PaymentMethod", saved.getId(), 
                String.format("Card ending in %s for user %d", saved.getCardLastFour(), userId));
        return mapToPaymentMethodResponse(saved);
    }

    @Override
    public List<PaymentMethodResponse> getUserPaymentMethods(Long userId) {
        appLogger.debug("Fetching payment methods for user: {}", userId);
        return paymentMethodRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(this::mapToPaymentMethodResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentMethodResponse getPaymentMethod(Long userId, Long paymentMethodId) {
        appLogger.debug("Fetching payment method {} for user {}", paymentMethodId, userId);
        
        UserPaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUserId(paymentMethodId, userId)
                .orElseThrow(() -> {
                    appLogger.logWarning("READ", "PaymentMethod", paymentMethodId, "Not found or access denied");
                    return PaymentException.paymentMethodNotFound(paymentMethodId);
                });
        return mapToPaymentMethodResponse(paymentMethod);
    }

    @Override
    @Transactional
    public void setDefaultPaymentMethod(Long userId, Long paymentMethodId) {
        appLogger.logStart("SET_DEFAULT", "PaymentMethod", paymentMethodId);
        
        UserPaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUserId(paymentMethodId, userId)
                .orElseThrow(() -> {
                    appLogger.logWarning("SET_DEFAULT", "PaymentMethod", paymentMethodId, "Not found");
                    return PaymentException.paymentMethodNotFound(paymentMethodId);
                });

        // Clear existing defaults
        List<UserPaymentMethod> existingMethods = paymentMethodRepository.findByUserIdAndIsActiveTrue(userId);
        existingMethods.forEach(pm -> pm.setIsDefault(false));
        paymentMethodRepository.saveAll(existingMethods);

        // Set new default
        paymentMethod.setIsDefault(true);
        paymentMethodRepository.save(paymentMethod);
        
        appLogger.logSuccess("SET_DEFAULT", "PaymentMethod", paymentMethodId, 
                "Set as default for user " + userId);
    }

    @Override
    @Transactional
    public void deletePaymentMethod(Long userId, Long paymentMethodId) {
        appLogger.logStart("DELETE", "PaymentMethod", paymentMethodId);
        
        UserPaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUserId(paymentMethodId, userId)
                .orElseThrow(() -> {
                    appLogger.logWarning("DELETE", "PaymentMethod", paymentMethodId, "Not found");
                    return PaymentException.paymentMethodNotFound(paymentMethodId);
                });

        paymentMethod.setIsActive(false);
        paymentMethodRepository.save(paymentMethod);
        
        appLogger.logSuccess("DELETE", "PaymentMethod", paymentMethodId, "(soft delete)");
    }

    // ===================== Payment Processing =====================

    @Override
    @Transactional
    public Payment preAuthorizePayment(Order order, Long paymentMethodId) {
        appLogger.logStart("PRE_AUTHORIZE", "Payment", order.getId());
        
        UserPaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> {
                    appLogger.logError("PRE_AUTHORIZE", "Payment", order.getId(), 
                            "Payment method not found: " + paymentMethodId);
                    return PaymentException.paymentMethodNotFound(paymentMethodId);
                });

        // Simulate IPG pre-authorization call
        // In production, this would call actual payment gateway
        String preAuthTransactionId = simulatePreAuthorization(paymentMethod, order.getTotalAmount());

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethodEntity(paymentMethod)
                .paymentMethod(paymentMethod.getPaymentMethod())
                .amount(order.getTotalAmount())
                .currency("LKR")
                .status(PaymentStatus.AUTHORIZED)
                .preAuthTransactionId(preAuthTransactionId)
                .authorizedAt(LocalDateTime.now())
                .gatewayResponse("Pre-authorization successful")
                .build();

        Payment saved = paymentRepository.save(payment);
        appLogger.logSuccess("PRE_AUTHORIZE", "Payment", saved.getId(), 
                String.format("Order %d, Amount %s, Transaction: %s", 
                        order.getId(), order.getTotalAmount(), preAuthTransactionId));
        return saved;
    }

    @Override
    @Transactional
    public Payment capturePayment(Long orderId) {
        appLogger.logStart("CAPTURE", "Payment", orderId);
        
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    appLogger.logError("CAPTURE", "Payment", orderId, "Payment not found");
                    return PaymentException.paymentNotFound(orderId);
                });

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw PaymentException.invalidPaymentStatus(
                    payment.getStatus().name(), 
                    PaymentStatus.AUTHORIZED.name()
            );
        }

        // Simulate IPG capture call
        String captureTransactionId = simulateCapture(payment.getPreAuthTransactionId(), payment.getAmount());

        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setCaptureTransactionId(captureTransactionId);
        payment.setCapturedAt(LocalDateTime.now());
        payment.setGatewayResponse("Payment captured successfully");

        Payment saved = paymentRepository.save(payment);
        log.info("Captured payment {} for order {}", saved.getId(), orderId);
        return saved;
    }

    @Override
    @Transactional
    public Payment releasePreAuthorization(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> PaymentException.paymentNotFound(orderId));

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw PaymentException.invalidPaymentStatus(
                    payment.getStatus().name(), 
                    PaymentStatus.AUTHORIZED.name()
            );
        }

        // Simulate IPG void/release call
        simulateReleaseAuthorization(payment.getPreAuthTransactionId());

        payment.setStatus(PaymentStatus.RELEASED);
        payment.setReleasedAt(LocalDateTime.now());
        payment.setGatewayResponse("Pre-authorization released");

        Payment saved = paymentRepository.save(payment);
        log.info("Released pre-authorization for payment {} order {}", saved.getId(), orderId);
        return saved;
    }

    @Override
    @Transactional
    public Payment refundPayment(Long orderId, String reason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> PaymentException.paymentNotFound(orderId));

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw PaymentException.invalidPaymentStatus(
                    payment.getStatus().name(), 
                    PaymentStatus.CAPTURED.name()
            );
        }

        // Simulate IPG refund call
        String refundTransactionId = simulateRefund(payment.getCaptureTransactionId(), payment.getAmount());

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundTransactionId(refundTransactionId);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setGatewayResponse("Refund processed: " + reason);

        Payment saved = paymentRepository.save(payment);
        log.info("Refunded payment {} for order {} reason: {}", saved.getId(), orderId, reason);
        return saved;
    }

    // ===================== Payment Queries =====================

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> PaymentException.paymentNotFound(orderId));
        return mapToPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentDetails(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found", "PAYMENT_NOT_FOUND"));
        return mapToPaymentResponse(payment);
    }

    // ===================== IPG Simulation Methods =====================
    // In production, these would call actual payment gateway APIs

    private String simulatePreAuthorization(UserPaymentMethod paymentMethod, java.math.BigDecimal amount) {
        // Simulate successful pre-authorization
        // In production: call IPG API with card token
        log.debug("Simulating pre-auth for card **** {} amount {}", paymentMethod.getCardLastFour(), amount);
        return "PRE_AUTH_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
    }

    private String simulateCapture(String preAuthTransactionId, java.math.BigDecimal amount) {
        // Simulate successful capture
        // In production: call IPG API to capture pre-authorized amount
        log.debug("Simulating capture for preAuth {} amount {}", preAuthTransactionId, amount);
        return "CAPTURE_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
    }

    private void simulateReleaseAuthorization(String preAuthTransactionId) {
        // Simulate release/void of pre-authorization
        // In production: call IPG API to void the pre-auth hold
        log.debug("Simulating release for preAuth {}", preAuthTransactionId);
    }

    private String simulateRefund(String captureTransactionId, java.math.BigDecimal amount) {
        // Simulate refund
        // In production: call IPG API to process refund
        log.debug("Simulating refund for capture {} amount {}", captureTransactionId, amount);
        return "REFUND_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
    }

    // ===================== Mappers =====================

    private PaymentMethodResponse mapToPaymentMethodResponse(UserPaymentMethod pm) {
        return PaymentMethodResponse.builder()
                .id(pm.getId())
                .paymentMethod(pm.getPaymentMethod())
                .cardLastFour(pm.getCardLastFour())
                .cardBrand(pm.getCardBrand())
                .cardExpiryMonth(pm.getCardExpiryMonth())
                .cardExpiryYear(pm.getCardExpiryYear())
                .cardHolderName(pm.getCardHolderName())
                .isDefault(pm.getIsDefault())
                .nickname(pm.getNickname())
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .preAuthTransactionId(payment.getPreAuthTransactionId())
                .captureTransactionId(payment.getCaptureTransactionId())
                .authorizedAt(payment.getAuthorizedAt())
                .capturedAt(payment.getCapturedAt())
                .failureReason(payment.getFailureReason())
                .build();
    }

    // ===================== Webhook Processing (SRS 10.7) =====================

    @Override
    @Transactional
    public void processWebhook(PaymentWebhookRequest webhookRequest) {
        log.info("Processing payment webhook for order reference: {}", webhookRequest.getOrderReference());
        
        // Validate webhook signature (in production, verify HMAC signature)
        if (!validateWebhookSignature(webhookRequest)) {
            log.warn("Invalid webhook signature for order reference: {}", webhookRequest.getOrderReference());
            throw new PaymentException("Invalid webhook signature", "INVALID_SIGNATURE");
        }
        
        // Find payment by transaction ID or order reference
        Payment payment = findPaymentFromWebhook(webhookRequest);
        if (payment == null) {
            log.warn("Payment not found for webhook: transactionId={}, orderRef={}", 
                    webhookRequest.getTransactionId(), webhookRequest.getOrderReference());
            throw new PaymentException("Payment not found", "PAYMENT_NOT_FOUND");
        }
        
        // Process based on status
        String status = webhookRequest.getStatus().toUpperCase();
        switch (status) {
            case "SUCCESS":
            case "APPROVED":
                handleSuccessfulPayment(payment, webhookRequest);
                break;
            case "FAILED":
            case "DECLINED":
                handleFailedPayment(payment, webhookRequest);
                break;
            case "CANCELLED":
            case "VOIDED":
                handleCancelledPayment(payment, webhookRequest);
                break;
            case "PENDING":
                log.info("Payment still pending for order: {}", payment.getOrder().getId());
                break;
            default:
                log.warn("Unknown payment status from webhook: {}", status);
        }
    }
    
    private boolean validateWebhookSignature(PaymentWebhookRequest webhookRequest) {
        // In production: validate HMAC signature using webhookSecret
        // For now, accept all requests (development mode)
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            log.warn("Webhook secret not configured - skipping signature validation");
            return true;
        }
        
        // TODO: Implement HMAC-SHA256 signature validation
        // String expectedSignature = calculateHmac(webhookRequest, webhookSecret);
        // return expectedSignature.equals(webhookRequest.getSignature());
        return true;
    }
    
    private Payment findPaymentFromWebhook(PaymentWebhookRequest webhookRequest) {
        // Try to find by transaction ID first
        if (webhookRequest.getTransactionId() != null) {
            Payment payment = paymentRepository.findByPreAuthTransactionId(webhookRequest.getTransactionId())
                    .orElse(null);
            if (payment != null) return payment;
            
            payment = paymentRepository.findByCaptureTransactionId(webhookRequest.getTransactionId())
                    .orElse(null);
            if (payment != null) return payment;
        }
        
        // Try to parse order reference as order ID
        if (webhookRequest.getOrderReference() != null) {
            try {
                Long orderId = Long.parseLong(webhookRequest.getOrderReference());
                return paymentRepository.findByOrderId(orderId).orElse(null);
            } catch (NumberFormatException e) {
                log.debug("Order reference is not a numeric ID: {}", webhookRequest.getOrderReference());
            }
        }
        
        return null;
    }
    
    private void handleSuccessfulPayment(Payment payment, PaymentWebhookRequest webhookRequest) {
        log.info("Processing successful payment webhook for order: {}", payment.getOrder().getId());
        
        // Update payment status based on current state
        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.AUTHORIZED);
            payment.setPreAuthTransactionId(webhookRequest.getTransactionId());
            payment.setAuthorizedAt(LocalDateTime.now());
            if (webhookRequest.getAuthorizationCode() != null) {
                payment.setAuthorizationCode(webhookRequest.getAuthorizationCode());
            }
        } else if (payment.getStatus() == PaymentStatus.AUTHORIZED) {
            // This might be a capture confirmation
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setCaptureTransactionId(webhookRequest.getTransactionId());
            payment.setCapturedAt(LocalDateTime.now());
        }
        
        paymentRepository.save(payment);
        log.info("Payment status updated to {} for order: {}", payment.getStatus(), payment.getOrder().getId());
    }
    
    private void handleFailedPayment(Payment payment, PaymentWebhookRequest webhookRequest) {
        log.info("Processing failed payment webhook for order: {}", payment.getOrder().getId());
        
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(webhookRequest.getErrorMessage() != null 
                ? webhookRequest.getErrorMessage() 
                : webhookRequest.getErrorCode());
        
        paymentRepository.save(payment);
        log.info("Payment marked as FAILED for order: {}", payment.getOrder().getId());
    }
    
    private void handleCancelledPayment(Payment payment, PaymentWebhookRequest webhookRequest) {
        log.info("Processing cancelled/voided payment webhook for order: {}", payment.getOrder().getId());
        
        // Use RELEASED for cancelled/voided pre-authorizations
        payment.setStatus(PaymentStatus.RELEASED);
        payment.setFailureReason("Payment cancelled/voided");
        payment.setReleasedAt(LocalDateTime.now());
        
        paymentRepository.save(payment);
        log.info("Payment marked as RELEASED for order: {}", payment.getOrder().getId());
    }
}
