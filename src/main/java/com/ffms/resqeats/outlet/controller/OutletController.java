package com.ffms.resqeats.outlet.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.geo.service.GeoService;
import com.ffms.resqeats.outlet.dto.CreateOutletRequest;
import com.ffms.resqeats.outlet.dto.OutletDto;
import com.ffms.resqeats.outlet.dto.OutletFilterDto;
import com.ffms.resqeats.outlet.dto.OutletListResponseDto;
import com.ffms.resqeats.outlet.dto.UpdateOutletRequest;
import com.ffms.resqeats.outlet.service.OutletService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.UserPrincipal;
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
 * Outlet controller per SRS Section 6.2.
 * 
 * All endpoints use unified paths - scope filtering is applied automatically
 * at the repository level based on the authenticated user's role and context.
 *
 * Public Endpoints:
 * GET /outlets/nearby - Find nearby outlets
 * GET /outlets/{id} - Get outlet details
 * GET /outlets - Search/browse outlets
 *
 * Merchant/Outlet Endpoints:
 * POST /merchants/{merchantId}/outlets - Create outlet (MERCHANT)
 * GET /merchants/{merchantId}/outlets - List merchant outlets (MERCHANT)
 * PUT /outlets/{id} - Update outlet
 * POST /outlets/{id}/hours - Set operating hours
 * POST /outlets/{id}/activate - Activate outlet (MERCHANT)
 * POST /outlets/{id}/deactivate - Deactivate outlet (MERCHANT)
 * POST /outlets/{id}/open - Open outlet (accept orders)
 * POST /outlets/{id}/close - Close outlet (stop orders)
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Outlets", description = "Outlet management APIs")
public class OutletController {

    private final OutletService outletService;

    // =====================
    // Public Endpoints
    // =====================

    @GetMapping("/outlets/nearby")
    @Operation(summary = "Find nearby outlets")
    public ResponseEntity<ApiResponse<List<GeoService.NearbyOutlet>>> getNearbyOutlets(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusKm) {
        log.info("Find nearby outlets request - lat: {}, lon: {}, radius: {}", latitude, longitude, radiusKm);
        try {
            List<GeoService.NearbyOutlet> outlets = outletService.getNearbyOutlets(latitude, longitude, radiusKm);
            log.info("Found {} nearby outlets", outlets.size());
            return ResponseEntity.ok(ApiResponse.success(outlets));
        } catch (Exception e) {
            log.error("Failed to find nearby outlets - Error: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/outlets/{id}")
    @Operation(summary = "Get outlet details")
    public ResponseEntity<ApiResponse<OutletDto>> getOutlet(@PathVariable Long id) {
        log.info("Get outlet details request for outletId: {}", id);
        try {
            OutletDto outlet = outletService.getOutlet(id);
            log.info("Successfully retrieved outlet: {}", id);
            return ResponseEntity.ok(ApiResponse.success(outlet));
        } catch (Exception e) {
            log.error("Failed to get outlet: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/outlets")
    @Operation(summary = "List outlets with filters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<OutletListResponseDto>>> getOutlets(
            OutletFilterDto filter,
            Pageable pageable) {
        log.info("List outlets request - filter: {}, page: {}", filter, pageable.getPageNumber());
        try {
            Page<OutletListResponseDto> outlets = outletService.getAllOutlets(filter, pageable);
            log.info("Retrieved {} outlets", outlets.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(outlets)));
        } catch (Exception e) {
            log.error("Failed to list outlets - Error: {}", e.getMessage());
            throw e;
        }
    }

    // =====================
    // Merchant Endpoints
    // =====================

    @PostMapping("/merchants/{merchantId}/outlets")
    @Operation(summary = "Create outlet")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<OutletDto>> createOutlet(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long merchantId,
            @Valid @RequestBody CreateOutletRequest request) {
        log.info("Create outlet request for merchantId: {} - Name: {}", merchantId, request.getName());
        try {
            OutletDto outlet = outletService.createOutlet(merchantId, request, currentUser.getId());
            log.info("Outlet created successfully: {} for merchant: {}", outlet.getId(), merchantId);
            return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet created"));
        } catch (Exception e) {
            log.error("Failed to create outlet for merchant: {} - Error: {}", merchantId, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/merchants/{merchantId}/outlets")
    @Operation(summary = "List merchant outlets")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<List<OutletDto>>> getMerchantOutlets(
            @PathVariable Long merchantId) {
        log.info("List outlets request for merchantId: {}", merchantId);
        try {
            List<OutletDto> outlets = outletService.getOutletsByMerchant(merchantId);
            log.info("Retrieved {} outlets for merchant: {}", outlets.size(), merchantId);
            return ResponseEntity.ok(ApiResponse.success(outlets));
        } catch (Exception e) {
            log.error("Failed to list outlets for merchant: {} - Error: {}", merchantId, e.getMessage());
            throw e;
        }
    }

    @PutMapping("/outlets/{id}")
    @Operation(summary = "Update outlet")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletDto>> updateOutlet(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOutletRequest request) {
        log.info("Update outlet request for outletId: {} by userId: {}", id, currentUser.getId());
        try {
            OutletDto outlet = outletService.updateOutlet(id, request, currentUser.getId());
            log.info("Outlet updated successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet updated"));
        } catch (Exception e) {
            log.error("Failed to update outlet: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlets/{id}/hours")
    @Operation(summary = "Set operating hours")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletDto>> setOperatingHours(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody List<CreateOutletRequest.OperatingHoursRequest> hours) {
        log.info("Set operating hours request for outletId: {}", id);
        try {
            OutletDto outlet = outletService.setOperatingHours(id, hours, currentUser.getId());
            log.info("Operating hours set successfully for outlet: {}", id);
            return ResponseEntity.ok(ApiResponse.success(outlet, "Operating hours updated"));
        } catch (Exception e) {
            log.error("Failed to set operating hours for outlet: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlets/{id}/activate")
    @Operation(summary = "Activate outlet")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<OutletDto>> activateOutlet(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        log.info("Activate outlet request for outletId: {} by userId: {}", id, currentUser.getId());
        try {
            OutletDto outlet = outletService.activateOutlet(id, currentUser.getId());
            log.info("Outlet activated successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet activated"));
        } catch (Exception e) {
            log.error("Failed to activate outlet: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlets/{id}/deactivate")
    @Operation(summary = "Deactivate outlet")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<OutletDto>> deactivateOutlet(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        log.info("Deactivate outlet request for outletId: {} by userId: {}", id, currentUser.getId());
        try {
            OutletDto outlet = outletService.deactivateOutlet(id, currentUser.getId());
            log.info("Outlet deactivated successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet deactivated"));
        } catch (Exception e) {
            log.error("Failed to deactivate outlet: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlets/{id}/open")
    @Operation(summary = "Open outlet (start accepting orders)")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletDto>> openOutlet(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        log.info("Open outlet request for outletId: {} by userId: {}", id, currentUser.getId());
        try {
            OutletDto outlet = outletService.reopenOutlet(id, currentUser.getId());
            log.info("Outlet opened successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet is now open"));
        } catch (Exception e) {
            log.error("Failed to open outlet: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/outlets/{id}/close")
    @Operation(summary = "Close outlet (stop accepting orders)")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletDto>> closeOutlet(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        log.info("Close outlet request for outletId: {} by userId: {}", id, currentUser.getId());
        try {
            OutletDto outlet = outletService.temporarilyCloseOutlet(id, currentUser.getId());
            log.info("Outlet closed successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet is now closed"));
        } catch (Exception e) {
            log.error("Failed to close outlet: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }
}
