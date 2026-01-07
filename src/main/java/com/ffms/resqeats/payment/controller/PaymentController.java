package com.ffms.resqeats.payment.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.payment.entity.Payment;
import com.ffms.resqeats.payment.entity.PaymentMethod;
import com.ffms.resqeats.payment.service.PaymentService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management APIs")
@PreAuthorize("hasRole('USER')")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/methods")
    @Operation(summary = "List user's payment methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getPaymentMethods(
            @CurrentUser UserPrincipal currentUser) {
        log.info("Get payment methods request for userId: {}", currentUser.getId());
        try {
            List<PaymentMethod> methods = paymentService.getUserPaymentMethods(currentUser.getId());
            log.info("Retrieved {} payment methods for userId: {}", methods.size(), currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(methods));
        } catch (Exception e) {
            log.error("Failed to get payment methods for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/methods")
    @Operation(summary = "Add payment method")
    public ResponseEntity<ApiResponse<PaymentMethod>> addPaymentMethod(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody PaymentService.AddPaymentMethodRequest request) {
        log.info("Add payment method request for userId: {} - setAsDefault: {}", currentUser.getId(), request.isSetAsDefault());
        try {
            PaymentMethod method = paymentService.addPaymentMethod(currentUser.getId(), request);
            log.info("Payment method added successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(method, "Payment method added"));
        } catch (Exception e) {
            log.error("Failed to add payment method for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/methods/{id}")
    @Operation(summary = "Remove payment method")
    public ResponseEntity<ApiResponse<Void>> removePaymentMethod(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id) {
        log.info("Remove payment method request for userId: {} - methodId: {}", currentUser.getId(), id);
        try {
            paymentService.removePaymentMethod(id, currentUser.getId());
            log.info("Payment method removed successfully: {} for userId: {}", id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Payment method removed"));
        } catch (Exception e) {
            log.error("Failed to remove payment method: {} for userId: {} - Error: {}", id, currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/methods/{id}/default")
    @Operation(summary = "Set default payment method")
    public ResponseEntity<ApiResponse<Void>> setDefaultPaymentMethod(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id) {
        log.info("Set default payment method request for userId: {} - methodId: {}", currentUser.getId(), id);
        try {
            paymentService.setDefaultPaymentMethod(id, currentUser.getId());
            log.info("Default payment method set successfully: {} for userId: {}", id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Default payment method updated"));
        } catch (Exception e) {
            log.error("Failed to set default payment method: {} for userId: {} - Error: {}", id, currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get payment for order")
    public ResponseEntity<ApiResponse<Payment>> getPayment(
            @PathVariable UUID orderId) {
        log.info("Get payment request for orderId: {}", orderId);
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            log.info("Payment retrieved successfully for orderId: {}", orderId);
            return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            log.error("Failed to get payment for orderId: {} - Error: {}", orderId, e.getMessage());
            throw e;
        }
    }
}
