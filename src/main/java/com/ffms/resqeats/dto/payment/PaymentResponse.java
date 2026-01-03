package com.ffms.resqeats.dto.payment;

import com.ffms.resqeats.enums.payment.PaymentMethod;
import com.ffms.resqeats.enums.payment.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String preAuthTransactionId;
    private String captureTransactionId;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private String failureReason;
}
