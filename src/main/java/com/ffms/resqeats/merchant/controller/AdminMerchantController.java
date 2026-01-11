package com.ffms.resqeats.merchant.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.merchant.dto.admin.AdminCreateMerchantRequest;
import com.ffms.resqeats.merchant.dto.admin.AdminUpdateMerchantRequest;
import com.ffms.resqeats.merchant.dto.admin.MerchantAdminDto;
import com.ffms.resqeats.merchant.dto.admin.MerchantAdminListDto;
import com.ffms.resqeats.merchant.dto.MerchantFilterDto;
import com.ffms.resqeats.merchant.dto.admin.MerchantLookupDto;
import com.ffms.resqeats.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin merchant controller per SRS Section 6.2.
 * <p>
 * Admin Endpoints:
 * POST /admin/merchants              - Create merchant
 * GET  /admin/merchants              - Filter merchants
 * GET  /admin/merchants/lookup       - Lookup merchants
 * GET  /admin/merchants/{id}         - Get merchant by ID
 * PUT  /admin/merchants/{id}         - Update merchant
 * POST /admin/merchants/{id}/approve - Approve merchant
 * POST /admin/merchants/{id}/reject  - Reject merchant
 * POST /admin/merchants/{id}/suspend - Suspend merchant
 */
@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Merchants", description = "Admin merchant management APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMerchantController {

    private final MerchantService merchantService;

    // =====================
    // Admin Query Endpoints
    // =====================

    @GetMapping("/{id}")
    @Operation(summary = "Get merchant by ID (Admin)")
    public ResponseEntity<ApiResponse<MerchantAdminDto>> getMerchantById(@PathVariable Long id) {
        log.info("Admin get merchant by ID: {}", id);
        MerchantAdminDto merchant = merchantService.getMerchantById(id);
        return ResponseEntity.ok(ApiResponse.success(merchant));
    }

    @GetMapping("/lookup")
    @Operation(summary = "Lookup merchants (Admin)")
    public ResponseEntity<ApiResponse<List<MerchantLookupDto>>> lookupMerchants(
            @RequestParam(required = false) String query) {
        log.info("Admin lookup merchants - query: {}", query);
        List<MerchantLookupDto> merchants = merchantService.lookupMerchants(query)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(merchants));
    }

    @GetMapping
    @Operation(summary = "Filter merchants (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<MerchantAdminListDto>>> filterMerchants(
            MerchantFilterDto filter,
            Pageable pageable) {
        log.info("Admin filter merchants - filter: {}, page: {}", filter, pageable.getPageNumber());
        Page<MerchantAdminListDto> merchants = merchantService.filterMerchants(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(merchants)));
    }

    // =====================
    // Admin Command Endpoints
    // =====================

    @PostMapping
    @Operation(summary = "Create merchant (Admin)")
    public ResponseEntity<ApiResponse<MerchantAdminDto>> createMerchant(
            @Valid @RequestBody AdminCreateMerchantRequest request) {
        log.info("Admin create merchant - name: {}", request.getName());
        MerchantAdminDto merchant = merchantService.createMerchant(request);
        return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update merchant (Admin)")
    public ResponseEntity<ApiResponse<MerchantAdminDto>> updateMerchant(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateMerchantRequest request) {
        log.info("Admin update merchant: {}", id);
        MerchantAdminDto merchant = merchantService.updateMerchant(id, request);
        return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant updated"));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve merchant (Admin)")
    public ResponseEntity<ApiResponse<MerchantAdminDto>> approveMerchant(@PathVariable Long id) {
        log.info("Admin approve merchant: {}", id);
        MerchantAdminDto merchant = merchantService.approveMerchant(id);
        return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant approved"));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject merchant (Admin)")
    public ResponseEntity<ApiResponse<MerchantAdminDto>> rejectMerchant(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request) {
        log.info("Admin reject merchant: {}", id);
        MerchantAdminDto merchant = merchantService.rejectMerchant(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant rejected"));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend merchant (Admin)")
    public ResponseEntity<ApiResponse<MerchantAdminDto>> suspendMerchant(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequest request) {
        log.info("Admin suspend merchant: {}", id);
        MerchantAdminDto merchant = merchantService.suspendMerchant(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant suspended"));
    }

    // =====================
    // Request DTOs
    // =====================

    @Data
    public static class RejectRequest {
        private String reason;
    }

    @Data
    public static class SuspendRequest {
        private String reason;
    }
}
