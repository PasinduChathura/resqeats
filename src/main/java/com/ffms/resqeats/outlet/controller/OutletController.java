package com.ffms.resqeats.outlet.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.geo.service.GeoService;
import com.ffms.resqeats.outlet.dto.CreateOutletRequest;
import com.ffms.resqeats.outlet.dto.UpdateOutletRequest;
import com.ffms.resqeats.outlet.dto.admin.OutletLookupDto;
import com.ffms.resqeats.outlet.dto.common.OperatingHoursDto;
import com.ffms.resqeats.outlet.dto.customer.OutletCustomerDTO;
import com.ffms.resqeats.outlet.dto.merchant.OutletMerchantDetailDTO;
import com.ffms.resqeats.outlet.dto.merchant.OutletMerchantListDTO;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import com.ffms.resqeats.outlet.service.OutletService;
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
import java.util.stream.Collectors;

/**
 * Public & Merchant-owned Outlet APIs per SRS Section 6.2.
 * <p>
 * Public Endpoints:
 * GET /outlets           - Search outlets (limited data)
 * GET /outlets/nearby    - Find nearby outlets (limited data)
 * GET /outlets/{id}      - Get outlet by ID (limited data)
 * <p>
 * Merchant Endpoints:
 * GET  /merchants/me/outlets           - Get my merchant's outlets (full data)
 * POST /merchants/me/outlets           - Create outlet for my merchant
 * PUT  /merchants/me/outlets/{id}      - Update my outlet
 * DELETE /merchants/me/outlets/{id}    - Delete my outlet
 * GET  /merchants/me/outlets/lookup    - Lookup my outlets for dropdown
 * POST /outlets/{id}/hours             - Set operating hours
 * POST /outlets/{id}/activate          - Activate outlet
 * POST /outlets/{id}/deactivate        - Deactivate outlet
 * POST /outlets/{id}/open              - Open outlet
 * POST /outlets/{id}/close             - Close outlet
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Outlets", description = "Public & Merchant Outlet APIs")
public class OutletController {

    private final OutletService outletService;

    // =====================
    // Public Endpoints
    // =====================

    @GetMapping("/outlets/{id}")
    @Operation(summary = "Get outlet by ID (limited data)")
    public ResponseEntity<ApiResponse<OutletCustomerDTO>> getPublicOutletById(@PathVariable Long id) {
        log.info("Public get outlet by ID: {}", id);
        OutletCustomerDTO outlet = outletService.getOutletCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(outlet));
    }

    @GetMapping("/outlets")
    @Operation(summary = "Search outlets (limited data)")
    public ResponseEntity<ApiResponse<PageResponse<OutletCustomerDTO>>> searchPublicOutlets(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            Pageable pageable) {
        log.info("Public search outlets - query: {}, city: {}", query, city);
        Page<OutletCustomerDTO> outlets = outletService.searchOutletsPublic(query, city, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(outlets)));
    }

    @GetMapping("/outlets/nearby")
    @Operation(summary = "Find nearby outlets")
    public ResponseEntity<ApiResponse<List<GeoService.NearbyOutlet>>> getNearbyOutlets(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusKm) {
        log.info("Find nearby outlets request - lat: {}, lon: {}, radius: {}", latitude, longitude, radiusKm);
        List<GeoService.NearbyOutlet> outlets = outletService.getNearbyOutlets(latitude, longitude, radiusKm);
        log.info("Found {} nearby outlets", outlets.size());
        return ResponseEntity.ok(ApiResponse.success(outlets));
    }

    @GetMapping("/outlets/statuses")
    @Operation(summary = "Get all supported outlet statuses")
    public ResponseEntity<ApiResponse<List<OutletStatus>>> getAllOutletStatuses() {
        return ResponseEntity.ok(ApiResponse.success(List.of(OutletStatus.values())));
    }

    // =====================
    // Merchant-Owned Endpoints
    // =====================

    @GetMapping("/merchants/me/outlets/{id}")
    @Operation(summary = "Get outlet by ID (Merchant - full data)")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<OutletMerchantDetailDTO>> getOutletById(@PathVariable Long id) {
        log.info("Merchant get outlet by ID: {}", id);
        OutletMerchantDetailDTO outlet = outletService.getOutletMerchantDetail(id);
        return ResponseEntity.ok(ApiResponse.success(outlet));
    }

    @GetMapping("/merchants/me/outlets")
    @Operation(summary = "Get my merchant's outlets (Merchant - full data)")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<PageResponse<OutletMerchantListDTO>>> getMyMerchantOutlets(Pageable pageable) {
        log.info("Get my merchant outlets, page: {}", pageable.getPageNumber());
        Page<OutletMerchantListDTO> outlets = outletService.getOutletsByCurrentUser(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(outlets)));
    }

    @GetMapping("/merchants/me/outlets/lookup")
    @Operation(summary = "Lookup my outlets (Merchant)")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<List<OutletLookupDto>>> lookupMyOutlets(
            @RequestParam(required = false) String query) {
        log.info("Lookup my outlets, query: {}", query);
        List<OutletLookupDto> outlets = outletService.lookupOutletsByCurrentUser(query)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(outlets));
    }

    @PostMapping("/merchants/me/outlets")
    @Operation(summary = "Create outlet for my merchant (Merchant)")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<OutletMerchantDetailDTO>> createMyOutlet(
            @Valid @RequestBody CreateOutletRequest request) {
        log.info("Merchant create outlet - name: {}", request.getName());
        OutletMerchantDetailDTO outlet = outletService.createOutletForCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet created"));
    }

    @PutMapping("/merchants/me/outlets/{id}")
    @Operation(summary = "Update my outlet (Merchant)")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<OutletMerchantDetailDTO>> updateMyOutlet(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOutletRequest request) {
        log.info("Merchant update outlet: {}", id);
        OutletMerchantDetailDTO outlet = outletService.updateOutlet(id, request);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet updated"));
    }

    @DeleteMapping("/merchants/me/outlets/{id}")
    @Operation(summary = "Delete my outlet (Merchant)")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<Void>> deleteMyOutlet(@PathVariable Long id) {
        log.info("Merchant delete outlet: {}", id);
        outletService.deleteOutlet(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Outlet deleted"));
    }

    // =====================
    // Outlet Operations (Merchant/Outlet User)
    // =====================

    @PostMapping("/outlets/{id}/hours")
    @Operation(summary = "Set operating hours")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletMerchantDetailDTO>> setOperatingHours(
            @PathVariable Long id,
            @Valid @RequestBody List<OperatingHoursDto> hours) {
        log.info("Set operating hours request for outletId: {}", id);
        OutletMerchantDetailDTO outlet = outletService.setOperatingHours(id, hours);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Operating hours updated"));
    }

    @PostMapping("/outlets/{id}/activate")
    @Operation(summary = "Activate outlet")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<OutletMerchantDetailDTO>> activateOutlet(@PathVariable Long id) {
        log.info("Activate outlet request for outletId: {}", id);
        OutletMerchantDetailDTO outlet = outletService.activateOutlet(id);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet activated"));
    }

    @PostMapping("/outlets/{id}/deactivate")
    @Operation(summary = "Deactivate outlet")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<OutletMerchantDetailDTO>> deactivateOutlet(@PathVariable Long id) {
        log.info("Deactivate outlet request for outletId: {}", id);
        OutletMerchantDetailDTO outlet = outletService.deactivateOutlet(id);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet deactivated"));
    }

    @PostMapping("/outlets/{id}/open")
    @Operation(summary = "Open outlet (start accepting orders)")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletMerchantDetailDTO>> openOutlet(@PathVariable Long id) {
        log.info("Open outlet request for outletId: {}", id);
        OutletMerchantDetailDTO outlet = outletService.reopenOutlet(id);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet is now open"));
    }

    @PostMapping("/outlets/{id}/close")
    @Operation(summary = "Close outlet (stop accepting orders)")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletMerchantDetailDTO>> closeOutlet(@PathVariable Long id) {
        log.info("Close outlet request for outletId: {}", id);
        OutletMerchantDetailDTO outlet = outletService.temporarilyCloseOutlet(id);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet is now closed"));
    }
}
