package com.ffms.resqeats.controller.payment;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.dto.payment.AddPaymentMethodRequest;
import com.ffms.resqeats.dto.payment.PaymentMethodResponse;
import com.ffms.resqeats.dto.payment.PaymentResponse;
import com.ffms.resqeats.dto.payment.PaymentWebhookRequest;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    // ===================== Payment Methods =====================

    @PostMapping("/methods")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> addPaymentMethod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddPaymentMethodRequest request) {
        PaymentMethodResponse response = paymentService.addPaymentMethod(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment method added successfully"));
    }

    @GetMapping("/methods")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PaymentMethodResponse> methods = paymentService.getUserPaymentMethods(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    @GetMapping("/methods/{methodId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getPaymentMethod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long methodId) {
        PaymentMethodResponse method = paymentService.getPaymentMethod(userDetails.getUserId(), methodId);
        return ResponseEntity.ok(ApiResponse.success(method));
    }

    @PutMapping("/methods/{methodId}/default")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> setDefaultPaymentMethod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long methodId) {
        paymentService.setDefaultPaymentMethod(userDetails.getUserId(), methodId);
        return ResponseEntity.ok(ApiResponse.success(null, "Default payment method updated"));
    }

    @DeleteMapping("/methods/{methodId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long methodId) {
        paymentService.deletePaymentMethod(userDetails.getUserId(), methodId);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment method removed"));
    }

    // ===================== Payment Details =====================

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(
            @PathVariable Long orderId) {
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentDetails(
            @PathVariable Long paymentId) {
        PaymentResponse payment = paymentService.getPaymentDetails(paymentId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    // ===================== Payment Webhook (IPG Callback) =====================

    /**
     * Webhook endpoint for payment gateway callbacks.
     * This endpoint is public (no auth) as it's called by the payment gateway.
     * Implements SRS 10.7 Payment Result Webhook.
     * 
     * The webhook validates the signature and processes:
     * - Pre-authorization results
     * - Capture confirmations
     * - Refund notifications
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handlePaymentWebhook(
            @RequestBody PaymentWebhookRequest webhookRequest) {
        log.info("Received payment webhook: transactionId={}, status={}, orderRef={}", 
                webhookRequest.getTransactionId(), 
                webhookRequest.getStatus(),
                webhookRequest.getOrderReference());
        
        try {
            paymentService.processWebhook(webhookRequest);
            return ResponseEntity.ok(ApiResponse.success(null, "Webhook processed successfully"));
        } catch (Exception e) {
            log.error("Failed to process payment webhook: {}", e.getMessage(), e);
            // Return 200 OK even on failure to prevent gateway retries for invalid requests
            // The payment service should handle retries for legitimate failures
            return ResponseEntity.ok(ApiResponse.error("Webhook processing failed: " + e.getMessage()));
        }
    }
}
