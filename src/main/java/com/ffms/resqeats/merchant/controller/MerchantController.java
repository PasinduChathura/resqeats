package com.ffms.resqeats.merchant.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.merchant.dto.customer.MerchantCustomerDto;
import com.ffms.resqeats.merchant.dto.customer.MerchantCustomerListDto;
import com.ffms.resqeats.merchant.dto.merchant.MerchantSelfDto;
import com.ffms.resqeats.merchant.dto.merchant.UpdateMyMerchantRequest;
import com.ffms.resqeats.merchant.enums.MerchantCategory;
import com.ffms.resqeats.merchant.enums.MerchantStatus;
import com.ffms.resqeats.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public & Merchant-owned Merchant APIs per SRS Section 6.2.
 * <p>
 * Public Endpoints:
 * GET /merchants           - Search merchants (limited data)
 * GET /merchants/{id}      - Get merchant by ID (limited data)
 * <p>
 * Merchant Endpoints:
 * GET /merchants/me        - Get my merchant (full data)
 * PUT /merchants/me        - Update my merchant
 */
@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merchants", description = "Public & Merchant APIs")
public class MerchantController {

    private final MerchantService merchantService;

    // =====================
    // Public Endpoints
    // =====================

    @GetMapping("/{id}")
    @Operation(summary = "Get merchant by ID (Public)")
    public ResponseEntity<ApiResponse<MerchantCustomerDto>> getPublicMerchantById(@PathVariable Long id) {
        log.info("Public get merchant by ID: {}", id);
        MerchantCustomerDto merchant = merchantService.getPublicMerchantById(id);
        return ResponseEntity.ok(ApiResponse.success(merchant));
    }

    @GetMapping
    @Operation(summary = "Search merchants (Public)")
    public ResponseEntity<ApiResponse<PageResponse<MerchantCustomerListDto>>> searchPublicMerchants(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        log.info("Public search merchants - query: {}", query);
        Page<MerchantCustomerListDto> merchants = (query != null && !query.isBlank())
                ? merchantService.searchMerchants(query, pageable)
                : merchantService.getMerchantsByStatus(MerchantStatus.APPROVED, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(merchants)));
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get all supported merchant statuses")
    public ResponseEntity<ApiResponse<List<MerchantStatus>>> getAllMerchantStatuses() {
        return ResponseEntity.ok(ApiResponse.success(List.of(MerchantStatus.values())));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all supported merchant categories")
    public ResponseEntity<ApiResponse<List<MerchantCategory>>> getAllMerchantCategories() {
        return ResponseEntity.ok(ApiResponse.success(List.of(MerchantCategory.values())));
    }

    // =====================
    // Merchant-Owned Endpoints
    // =====================

    @GetMapping("/me")
    @Operation(summary = "Get my merchant (Merchant)")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<MerchantSelfDto>> getMyMerchant() {
        log.info("Get my merchant");
        MerchantSelfDto merchant = merchantService.getMyMerchant();
        return ResponseEntity.ok(ApiResponse.success(merchant));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my merchant (Merchant)")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<MerchantSelfDto>> updateMyMerchant(
            @Valid @RequestBody UpdateMyMerchantRequest request) {
        log.info("Update my merchant");
        MerchantSelfDto merchant = merchantService.updateMyMerchant(request);
        return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant updated"));
    }
}
