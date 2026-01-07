package com.ffms.resqeats.merchant.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.merchant.dto.CreateMerchantRequest;
import com.ffms.resqeats.merchant.dto.MerchantDto;
import com.ffms.resqeats.merchant.dto.UpdateMerchantRequest;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import com.ffms.resqeats.merchant.service.MerchantService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Merchant controller per SRS Section 6.2.
 *
 * Public Endpoints:
 * GET /merchants/{id} - Get merchant details
 * GET /merchants - Search merchants
 *
 * User Endpoints:
 * POST /merchants - Register as merchant
 *
 * Merchant Endpoints:
 * GET /merchants/me - Get my merchant
 * PUT /merchants/me - Update my merchant
 *
 * Admin Endpoints:
 * GET /admin/merchants - List all merchants
 * POST /admin/merchants/{id}/approve - Approve merchant
 * POST /admin/merchants/{id}/reject - Reject merchant
 * POST /admin/merchants/{id}/suspend - Suspend merchant
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merchants", description = "Merchant management APIs")
public class MerchantController {

    private final MerchantService merchantService;

    // =====================
    // Public Endpoints
    // =====================

    @GetMapping("/merchants/{id}")
    @Operation(summary = "Get merchant details")
    public ResponseEntity<ApiResponse<MerchantDto>> getMerchant(@PathVariable UUID id) {
        log.info("Get merchant details request for merchantId: {}", id);
        try {
            MerchantDto merchant = merchantService.getMerchant(id);
            log.info("Successfully retrieved merchant: {}", id);
            return ResponseEntity.ok(ApiResponse.success(merchant));
        } catch (Exception e) {
            log.error("Failed to get merchant: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/merchants")
    @Operation(summary = "Search merchants")
    public ResponseEntity<ApiResponse<PageResponse<MerchantDto>>> searchMerchants(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        log.info("Search merchants request - query: {}, page: {}", query, pageable.getPageNumber());
        try {
            Page<MerchantDto> merchants = query != null 
                    ? merchantService.searchMerchants(query, pageable)
                    : merchantService.getMerchantsByStatus(MerchantStatus.APPROVED, pageable);
            log.info("Found {} merchants", merchants.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(merchants)));
        } catch (Exception e) {
            log.error("Failed to search merchants - Error: {}", e.getMessage());
            throw e;
        }
    }

    // =====================
    // User Endpoints
    // =====================

    @PostMapping("/merchants")
    @Operation(summary = "Register as merchant")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<MerchantDto>> registerMerchant(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateMerchantRequest request) {
        log.info("Register merchant request from userId: {} - Business: {}", currentUser.getId(), request.getName());
        try {
            MerchantDto merchant = merchantService.registerMerchant(request, currentUser.getId());
            log.info("Merchant registered successfully: {} by userId: {}", merchant.getId(), currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant registration submitted for approval"));
        } catch (Exception e) {
            log.error("Failed to register merchant for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    // =====================
    // Merchant Endpoints
    // =====================

    @GetMapping("/merchants/me")
    @Operation(summary = "Get my merchant")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<MerchantDto>> getMyMerchant(@CurrentUser UserPrincipal currentUser) {
        log.info("Get my merchant request from userId: {}", currentUser.getId());
        try {
            MerchantDto merchant = merchantService.getMerchantByOwner(currentUser.getId());
            log.info("Successfully retrieved merchant for owner: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(merchant));
        } catch (Exception e) {
            log.error("Failed to get merchant for owner: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/merchants/me")
    @Operation(summary = "Update my merchant")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<MerchantDto>> updateMyMerchant(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateMerchantRequest request) {
        log.info("Update my merchant request from userId: {}", currentUser.getId());
        try {
            MerchantDto merchant = merchantService.getMerchantByOwner(currentUser.getId());
            MerchantDto updated = merchantService.updateMerchant(merchant.getId(), request, currentUser.getId());
            log.info("Merchant updated successfully: {}", merchant.getId());
            return ResponseEntity.ok(ApiResponse.success(updated, "Merchant updated"));
        } catch (Exception e) {
            log.error("Failed to update merchant for owner: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    // =====================
    // Admin Endpoints
    // =====================

    @GetMapping("/admin/merchants")
    @Operation(summary = "List all merchants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<MerchantDto>>> getAllMerchants(
            @RequestParam(required = false) MerchantStatus status,
            Pageable pageable) {
        log.info("Admin: List all merchants request - status: {}, page: {}", status, pageable.getPageNumber());
        try {
            Page<MerchantDto> merchants = status != null
                    ? merchantService.getMerchantsByStatus(status, pageable)
                    : merchantService.getAllMerchants(pageable);
            log.info("Admin: Retrieved {} merchants", merchants.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(merchants)));
        } catch (Exception e) {
            log.error("Admin: Failed to list merchants - Error: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/admin/merchants/{id}/approve")
    @Operation(summary = "Approve merchant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MerchantDto>> approveMerchant(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id) {
        log.info("Admin: Approve merchant request for merchantId: {} by adminId: {}", id, currentUser.getId());
        try {
            MerchantDto merchant = merchantService.approveMerchant(id, currentUser.getId());
            log.info("Admin: Merchant approved successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant approved"));
        } catch (Exception e) {
            log.error("Admin: Failed to approve merchant: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/admin/merchants/{id}/reject")
    @Operation(summary = "Reject merchant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MerchantDto>> rejectMerchant(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody RejectRequest request) {
        log.info("Admin: Reject merchant request for merchantId: {} - Reason: {}", id, request.getReason());
        try {
            MerchantDto merchant = merchantService.rejectMerchant(id, currentUser.getId(), request.getReason());
            log.info("Admin: Merchant rejected: {}", id);
            return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant rejected"));
        } catch (Exception e) {
            log.error("Admin: Failed to reject merchant: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/admin/merchants/{id}/suspend")
    @Operation(summary = "Suspend merchant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MerchantDto>> suspendMerchant(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody SuspendRequest request) {
        log.info("Admin: Suspend merchant request for merchantId: {} - Reason: {}", id, request.getReason());
        try {
            MerchantDto merchant = merchantService.suspendMerchant(id, currentUser.getId(), request.getReason());
            log.info("Admin: Merchant suspended: {}", id);
            return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant suspended"));
        } catch (Exception e) {
            log.error("Admin: Failed to suspend merchant: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // Request DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectRequest {
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuspendRequest {
        private String reason;
    }
}
