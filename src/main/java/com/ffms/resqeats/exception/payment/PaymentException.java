package com.ffms.resqeats.exception.payment;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
    private final String errorCode;

    public PaymentException(String message) {
        super(message);
        this.errorCode = "PAYMENT_ERROR";
    }

    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public static PaymentException paymentMethodNotFound(Long id) {
        return new PaymentException("Payment method not found with id: " + id, "PAYMENT_METHOD_NOT_FOUND");
    }

    public static PaymentException paymentNotFound(Long orderId) {
        return new PaymentException("Payment not found for order: " + orderId, "PAYMENT_NOT_FOUND");
    }

    public static PaymentException preAuthFailed(String reason) {
        return new PaymentException("Pre-authorization failed: " + reason, "PRE_AUTH_FAILED");
    }

    public static PaymentException captureFailed(String reason) {
        return new PaymentException("Payment capture failed: " + reason, "CAPTURE_FAILED");
    }

    public static PaymentException refundFailed(String reason) {
        return new PaymentException("Refund failed: " + reason, "REFUND_FAILED");
    }

    public static PaymentException invalidPaymentStatus(String currentStatus, String requiredStatus) {
        return new PaymentException("Invalid payment status. Current: " + currentStatus + ", Required: " + requiredStatus, "INVALID_PAYMENT_STATUS");
    }
}
