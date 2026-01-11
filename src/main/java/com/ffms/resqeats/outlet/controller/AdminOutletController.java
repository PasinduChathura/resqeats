package com.ffms.resqeats.outlet.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.outlet.dto.OutletFilterDto;
import com.ffms.resqeats.outlet.dto.admin.AdminUpdateOutletRequest;
import com.ffms.resqeats.outlet.dto.admin.AdminCreateOutletRequest;
import com.ffms.resqeats.outlet.dto.admin.OutletAdminDetailDTO;
import com.ffms.resqeats.outlet.dto.admin.OutletAdminListDTO;
import com.ffms.resqeats.outlet.dto.admin.OutletLookupDto;
import com.ffms.resqeats.outlet.service.OutletService;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin outlet controller per SRS Section 6.2.
 * <p>
 * Admin Endpoints:
 * POST   /admin/outlets              - Create outlet for any merchant
 * GET    /admin/outlets              - Filter outlets (all merchants)
 * GET    /admin/outlets/lookup       - Lookup outlets for dropdown
 * GET    /admin/outlets/{id}         - Get outlet by ID (full details)
 * PUT    /admin/outlets/{id}         - Update outlet
 * DELETE /admin/outlets/{id}         - Delete outlet
 * POST   /admin/outlets/{id}/approve - Approve outlet
 * POST   /admin/outlets/{id}/suspend - Suspend outlet
 * POST   /admin/outlets/{id}/activate - Activate outlet
 */
@RestController
@RequestMapping("/admin/outlets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Outlets", description = "Admin outlet management APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOutletController {

    private final OutletService outletService;

    // =====================
    // Admin Query Endpoints
    // =====================

    @GetMapping("/{id}")
    @Operation(summary = "Get outlet by ID (Admin)")
    public ResponseEntity<ApiResponse<OutletAdminDetailDTO>> getOutletById(@PathVariable Long id) {
        log.info("Admin get outlet by ID: {}", id);
        OutletAdminDetailDTO outlet = outletService.getOutletAdminDetail(id);
        return ResponseEntity.ok(ApiResponse.success(outlet));
    }

    @GetMapping("/lookup")
    @Operation(summary = "Lookup outlets (Admin)")
    public ResponseEntity<ApiResponse<List<OutletLookupDto>>> lookupOutlets(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long merchantId) {
        log.info("Admin lookup outlets - query: {}, merchantId: {}", query, merchantId);
        List<OutletLookupDto> outlets = outletService.lookupOutlets(query, merchantId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(outlets));
    }

    @GetMapping
    @Operation(summary = "Filter outlets (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<OutletAdminListDTO>>> filterOutlets(
            OutletFilterDto filter,
            Pageable pageable) {
        log.info("Admin filter outlets - filter: {}, page: {}", filter, pageable.getPageNumber());
        Page<OutletAdminListDTO> outlets = outletService.filterOutletsAdmin(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(outlets)));
    }

    // =====================
    // Admin Command Endpoints
    // =====================

    @PostMapping
    @Operation(summary = "Create outlet (Admin)")
    public ResponseEntity<ApiResponse<OutletAdminDetailDTO>> createOutlet(
            @Valid @RequestBody AdminCreateOutletRequest request) {
        log.info("Admin create outlet - name: {}, merchantId: {}", request.getName(), request.getMerchantId());
        OutletAdminDetailDTO outlet = outletService.createOutletAdmin(request);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update outlet (Admin)")
    public ResponseEntity<ApiResponse<OutletAdminDetailDTO>> updateOutlet(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateOutletRequest request) {
        log.info("Admin update outlet: {}", id);
        OutletAdminDetailDTO outlet = outletService.updateOutletAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete outlet (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteOutlet(@PathVariable Long id) {
        log.info("Admin delete outlet: {}", id);
        outletService.deleteOutlet(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Outlet deleted"));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve outlet (Admin)")
    public ResponseEntity<ApiResponse<OutletAdminDetailDTO>> approveOutlet(@PathVariable Long id) {
        log.info("Admin approve outlet: {}", id);
        OutletAdminDetailDTO outlet = outletService.approveOutlet(id);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet approved"));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend outlet (Admin)")
    public ResponseEntity<ApiResponse<OutletAdminDetailDTO>> suspendOutlet(
            @PathVariable Long id,
            @Valid @RequestBody SuspendRequest request) {
        log.info("Admin suspend outlet: {}", id);
        OutletAdminDetailDTO outlet = outletService.suspendOutlet(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet suspended"));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate outlet (Admin)")
    public ResponseEntity<ApiResponse<OutletAdminDetailDTO>> activateOutlet(@PathVariable Long id) {
        log.info("Admin activate outlet: {}", id);
        OutletAdminDetailDTO outlet = outletService.activateOutletAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet activated"));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate outlet (Admin)")
    public ResponseEntity<ApiResponse<OutletAdminDetailDTO>> deactivateOutlet(@PathVariable Long id) {
        log.info("Admin deactivate outlet: {}", id);
        OutletAdminDetailDTO outlet = outletService.deactivateOutletAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(outlet, "Outlet deactivated"));
    }

    // =====================
    // Request DTOs
    // =====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuspendRequest {
        private String reason;
    }
}
