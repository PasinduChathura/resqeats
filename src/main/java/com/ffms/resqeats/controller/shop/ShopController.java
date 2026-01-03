package com.ffms.resqeats.controller.shop;

import com.ffms.resqeats.dto.shop.*;
import com.ffms.resqeats.enums.shop.ShopCategory;
import com.ffms.resqeats.enums.shop.ShopStatus;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.service.shop.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /**
     * Create a new shop (Shop Owner only)
     */
    @PostMapping
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.writePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ShopResponse> createShop(
            @Valid @RequestBody CreateShopRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ShopResponse response = shopService.createShop(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a shop (Shop Owner only - own shops)
     */
    @PutMapping("/{shopId}")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ShopResponse> updateShop(
            @PathVariable Long shopId,
            @Valid @RequestBody UpdateShopRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ShopResponse response = shopService.updateShop(shopId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get shop by ID (Public for approved shops)
     */
    @GetMapping("/{shopId}")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ShopResponse> getShopById(@PathVariable Long shopId) {
        ShopResponse response = shopService.getShopById(shopId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all shops for owner
     */
    @GetMapping("/my-shops")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<List<ShopResponse>> getMyShops(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ShopResponse> shops = shopService.getShopsByOwner(userDetails.getId());
        return ResponseEntity.ok(shops);
    }

    /**
     * Get all shops with filters (Admin)
     */
    @GetMapping
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<Page<ShopResponse>> getAllShops(
            @RequestParam(required = false) ShopStatus status,
            @RequestParam(required = false) ShopCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ShopResponse> shops = shopService.getAllShops(status, category, pageable);
        return ResponseEntity.ok(shops);
    }

    /**
     * Get nearby shops (User location-based discovery)
     */
    @GetMapping("/nearby")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<List<ShopResponse>> getNearbyShops(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "5.0") Double radius,
            @RequestParam(required = false) String category) {
        List<ShopResponse> shops = shopService.getNearbyShops(lat, lng, radius, category);
        return ResponseEntity.ok(shops);
    }

    /**
     * Get nearby shops with pagination
     */
    @GetMapping("/nearby/page")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<Page<ShopResponse>> getNearbyShopsWithPagination(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "5.0") Double radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShopResponse> shops = shopService.getNearbyShopsWithPagination(lat, lng, radius, pageable);
        return ResponseEntity.ok(shops);
    }

    /**
     * Approve a shop (Admin only)
     */
    @PostMapping("/{shopId}/approve")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ShopResponse> approveShop(@PathVariable Long shopId) {
        ShopResponse response = shopService.approveShop(shopId);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject a shop (Admin only)
     */
    @PostMapping("/{shopId}/reject")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ShopResponse> rejectShop(
            @PathVariable Long shopId,
            @RequestParam String reason) {
        ShopResponse response = shopService.rejectShop(shopId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Suspend a shop (Admin only)
     */
    @PostMapping("/{shopId}/suspend")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ShopResponse> suspendShop(
            @PathVariable Long shopId,
            @RequestParam String reason) {
        ShopResponse response = shopService.suspendShop(shopId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Toggle shop open/closed status (Shop Owner only)
     */
    @PostMapping("/{shopId}/toggle-status")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ShopResponse> toggleShopStatus(
            @PathVariable Long shopId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ShopResponse response = shopService.toggleShopOpenStatus(shopId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a shop (Shop Owner only - own shops)
     */
    @DeleteMapping("/{shopId}")
    @PreAuthorize("hasPermission(#id, @appUtils.shopResource, @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<Void> deleteShop(
            @PathVariable Long shopId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        shopService.deleteShop(shopId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
