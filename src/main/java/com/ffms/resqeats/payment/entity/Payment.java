package com.ffms.resqeats.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import com.ffms.resqeats.payment.enums.PaymentStatus;
import com.ffms.resqeats.security.tenant.TenantScoped;
import com.ffms.resqeats.security.tenant.TenantScopeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity per SRS Section 7.2.
 * Implements payment pre-authorization and capture flow per SRS Section 4.5.
 *
 * Payment Flow (SRS Section 6.10):
 * 1. Card Registration → Token stored (IPG)
 * 2. Checkout → Pre-authorization (hold funds)
 * 3. Outlet Accept → Capture (transfer funds)
 * 4. Outlet Decline/Timeout → Void (release hold)
 * 
 * TENANT SCOPED: Payments are scoped through their associated Order.
 * Access control enforced at service layer via order ownership.
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_ipg_txn", columnList = "ipg_transaction_id")
})
@FilterDef(name = "paymentOrderFilter", parameters = @ParamDef(name = "orderId", type = Long.class))
@Filter(name = "paymentOrderFilter", condition = "order_id = :orderId")
@TenantScoped(value = TenantScopeType.USER, allowNull = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @NotNull
    @Column(name = "order_id", nullable = false, unique = true)
    @JsonProperty("order_id")
    private Long orderId;

    @NotNull
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    @JsonProperty("amount")
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    @JsonProperty("currency")
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @JsonProperty("status")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Tokenized payment method reference.
     */
    @Column(name = "payment_method_token", length = 255)
    @JsonProperty("payment_method_token")
    private String paymentMethodToken;

    /**
     * Pre-authorization code from IPG.
     */
    @Column(name = "authorization_code", length = 100)
    @JsonProperty("authorization_code")
    private String authorizationCode;

    /**
     * Capture code from IPG (after successful capture).
     */
    @Column(name = "capture_code", length = 100)
    @JsonProperty("capture_code")
    private String captureCode;

    /**
     * IPG transaction reference ID.
     */
    @Column(name = "ipg_transaction_id", length = 100)
    @JsonProperty("ipg_transaction_id")
    private String ipgTransactionId;

    /**
     * Refund transaction ID (if refunded).
     */
    @Column(name = "refund_transaction_id", length = 100)
    @JsonProperty("refund_transaction_id")
    private String refundTransactionId;

    /**
     * Raw IPG response for debugging/audit.
     */
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    @JsonProperty("gateway_response")
    private String gatewayResponse;

    /**
     * Failure reason if payment failed.
     */
    @Column(name = "failure_reason", length = 500)
    @JsonProperty("failure_reason")
    private String failureReason;

    // Timestamps
    @Column(name = "authorized_at")
    @JsonProperty("authorized_at")
    private LocalDateTime authorizedAt;

    @Column(name = "captured_at")
    @JsonProperty("captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "voided_at")
    @JsonProperty("voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "refunded_at")
    @JsonProperty("refunded_at")
    private LocalDateTime refundedAt;

    /**
     * Payment method ID reference.
     */
    @Column(name = "payment_method_id")
    @JsonProperty("payment_method_id")
    private Long paymentMethodId;

    /**
     * Idempotency key to prevent duplicate operations.
     * MEDIUM FIX (Issue #12): Added for idempotent payment operations.
     */
    @Column(name = "idempotency_key", length = 100, unique = true)
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    /**
     * Check if payment can be captured.
     */
    public boolean canBeCaptured() {
        return status == PaymentStatus.AUTHORIZED;
    }

    /**
     * Check if payment can be voided.
     */
    public boolean canBeVoided() {
        return status == PaymentStatus.AUTHORIZED;
    }

    /**
     * Check if payment can be refunded.
     */
    public boolean canBeRefunded() {
        return status == PaymentStatus.CAPTURED;
    }
}
