package com.ffms.resqeats.service.payment;

import com.ffms.resqeats.dto.payment.AddPaymentMethodRequest;
import com.ffms.resqeats.dto.payment.PaymentMethodResponse;
import com.ffms.resqeats.dto.payment.PaymentResponse;
import com.ffms.resqeats.dto.payment.PaymentWebhookRequest;
import com.ffms.resqeats.models.order.Order;
import com.ffms.resqeats.models.payment.Payment;

import java.util.List;

public interface PaymentService {

    // Payment Method Management
    PaymentMethodResponse addPaymentMethod(Long userId, AddPaymentMethodRequest request);

    List<PaymentMethodResponse> getUserPaymentMethods(Long userId);

    PaymentMethodResponse getPaymentMethod(Long userId, Long paymentMethodId);

    void setDefaultPaymentMethod(Long userId, Long paymentMethodId);

    void deletePaymentMethod(Long userId, Long paymentMethodId);

    // Payment Processing - Deferred Capture Flow
    Payment preAuthorizePayment(Order order, Long paymentMethodId);

    Payment capturePayment(Long orderId);

    Payment releasePreAuthorization(Long orderId);

    Payment refundPayment(Long orderId, String reason);

    // Payment Queries
    PaymentResponse getPaymentByOrderId(Long orderId);

    PaymentResponse getPaymentDetails(Long paymentId);

    // Payment Webhook Processing (SRS 10.7)
    void processWebhook(PaymentWebhookRequest webhookRequest);
}
