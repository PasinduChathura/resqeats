package com.ffms.resqeats.models.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import com.ffms.resqeats.enums.payment.PaymentMethod;
import com.ffms.resqeats.enums.payment.PaymentStatus;
import com.ffms.resqeats.models.order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private UserPaymentMethod paymentMethodEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", columnDefinition = "VARCHAR(30)")
    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(30)")
    @JsonProperty("status")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    @JsonProperty("amount")
    private BigDecimal amount;

    @Column(name = "currency", columnDefinition = "VARCHAR(3)")
    @JsonProperty("currency")
    @Builder.Default
    private String currency = "LKR";

    @Column(name = "pre_auth_transaction_id", columnDefinition = "VARCHAR(255)")
    @JsonProperty("pre_auth_transaction_id")
    private String preAuthTransactionId;

    @Column(name = "capture_transaction_id", columnDefinition = "VARCHAR(255)")
    @JsonProperty("capture_transaction_id")
    private String captureTransactionId;

    @Column(name = "refund_transaction_id", columnDefinition = "VARCHAR(255)")
    @JsonProperty("refund_transaction_id")
    private String refundTransactionId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    @JsonProperty("gateway_response")
    private String gatewayResponse;

    @Column(name = "authorized_at")
    @JsonProperty("authorized_at")
    private LocalDateTime authorizedAt;

    @Column(name = "captured_at")
    @JsonProperty("captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "released_at")
    @JsonProperty("released_at")
    private LocalDateTime releasedAt;

    @Column(name = "refunded_at")
    @JsonProperty("refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "failure_reason", columnDefinition = "VARCHAR(500)")
    @JsonProperty("failure_reason")
    private String failureReason;

    @Column(name = "authorization_code", columnDefinition = "VARCHAR(50)")
    @JsonProperty("authorization_code")
    private String authorizationCode;
}
