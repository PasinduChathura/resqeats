package com.ffms.resqeats.payment.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.payment.entity.Payment;
import com.ffms.resqeats.payment.entity.PaymentMethod;
import com.ffms.resqeats.payment.service.PaymentService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payment controller per SRS Section 6.2.
 *
 * Endpoints:
 * GET /payments/methods - List user's payment methods
 * POST /payments/methods - Add payment method
 * DELETE /payments/methods/{id} - Remove payment method
 * PUT /payments/methods/{id}/default - Set default payment method
 * GET /payments/{orderId} - Get payment for order
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management APIs")
@PreAuthorize("hasRole('CUSTOMER_USER')")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/methods")
    @Operation(summary = "List user's payment methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getPaymentMethods(
            @CurrentUser CustomUserDetails currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }
        List<PaymentMethod> methods = paymentService.getUserPaymentMethods(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    @PostMapping("/methods")
    @Operation(summary = "Add payment method")
    public ResponseEntity<ApiResponse<PaymentMethod>> addPaymentMethod(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody PaymentService.AddPaymentMethodRequest request) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }
        PaymentMethod method = paymentService.addPaymentMethod(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(method, "Payment method added"));
    }

    @DeleteMapping("/methods/{id}")
    @Operation(summary = "Remove payment method")
    public ResponseEntity<ApiResponse<Void>> removePaymentMethod(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }
        paymentService.removePaymentMethod(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Payment method removed"));
    }

    @PutMapping("/methods/{id}/default")
    @Operation(summary = "Set default payment method")
    public ResponseEntity<ApiResponse<Void>> setDefaultPaymentMethod(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication required");
        }
        paymentService.setDefaultPaymentMethod(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Default payment method updated"));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get payment for order")
    public ResponseEntity<ApiResponse<Payment>> getPayment(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }
}
