package com.ffms.resqeats.payment.dto;

import com.ffms.resqeats.payment.enums.PaymentMethodType;
import com.ffms.resqeats.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private PaymentMethodType paymentMethodType;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String preAuthTransactionId;
    private String captureTransactionId;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private String failureReason;
}
