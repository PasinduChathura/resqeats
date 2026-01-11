package com.ffms.resqeats.item.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.item.dto.CreateItemRequest;
import com.ffms.resqeats.item.dto.ItemDto;
import com.ffms.resqeats.item.dto.ItemFilterDto;
import com.ffms.resqeats.item.dto.ItemListResponseDto;
import com.ffms.resqeats.item.dto.OutletItemDto;
import com.ffms.resqeats.item.dto.UpdateItemRequest;
import com.ffms.resqeats.item.service.ItemService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.CustomUserDetails;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Item controller per SRS Section 6.2.
 * 
 * All endpoints use unified paths - scope filtering is applied automatically
 * at the repository level based on the authenticated user's role and context.
 *
 * Public Endpoints:
 * GET /outlets/{outletId}/items - Get outlet items (customer view)
 * GET /items/search - Search items
 * GET /items/{id} - Get item details
 *
 * Scoped Endpoints:
 * GET /items - List items with filters (scoped by role)
 * POST /merchants/{merchantId}/items - Create item
 * GET /merchants/{merchantId}/items - List merchant items
 * PUT /items/{id} - Update item
 * POST /outlets/{outletId}/items - Add item to outlet
 * PUT /outlets/{outletId}/items/{outletItemId} - Update outlet item
 * DELETE /outlets/{outletId}/items/{outletItemId} - Remove from outlet
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Items", description = "Item management APIs")
public class ItemController {

    private final ItemService itemService;

    // =====================
    // Public Endpoints
    // =====================

    @GetMapping("/outlets/{outletId}/items")
    @Operation(summary = "Get outlet items (customer view)")
    public ResponseEntity<ApiResponse<List<OutletItemDto>>> getOutletItems(@PathVariable Long outletId) {
        List<OutletItemDto> items = itemService.getAvailableOutletItems(outletId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/items/search")
    @Operation(summary = "Search items")
    public ResponseEntity<ApiResponse<PageResponse<OutletItemDto>>> searchItems(
            @RequestParam String query,
            Pageable pageable) {
        Page<OutletItemDto> items = itemService.searchItems(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(items)));
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get item details")
    public ResponseEntity<ApiResponse<ItemDto>> getItem(@PathVariable Long id) {
        ItemDto item = itemService.getItem(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    // =====================
    // Merchant Endpoints
    // =====================

    @PostMapping("/merchants/{merchantId}/items")
    @Operation(summary = "Create item")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<ItemDto>> createItem(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long merchantId,
            @Valid @RequestBody CreateItemRequest request) {
        ItemDto item = itemService.createItem(merchantId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(item, "Item created"));
    }

    @GetMapping("/merchants/{merchantId}/items")
    @Operation(summary = "List merchant items")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<PageResponse<ItemDto>>> getMerchantItems(
            @PathVariable Long merchantId,
            Pageable pageable) {
        Page<ItemDto> items = itemService.getMerchantItems(merchantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(items)));
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update item")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<ItemDto>> updateItem(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        ItemDto item = itemService.updateItem(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(item, "Item updated"));
    }

    // =====================
    // Outlet Item Endpoints
    // =====================

    @PostMapping("/outlets/{outletId}/items")
    @Operation(summary = "Add item to outlet")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletItemDto>> addItemToOutlet(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long outletId,
            @Valid @RequestBody AddItemToOutletRequest request) {
        OutletItemDto outletItem = itemService.addItemToOutlet(
                outletId,
                request.getItemId(),
                request.getQuantity() != null ? request.getQuantity() : 0,
                currentUser.getId()
        );
        return ResponseEntity.ok(ApiResponse.success(outletItem, "Item added to outlet"));
    }

    @PutMapping("/outlets/{outletId}/items/{outletItemId}")
    @Operation(summary = "Update outlet item")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletItemDto>> updateOutletItem(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long outletId,
            @PathVariable Long outletItemId,
            @Valid @RequestBody UpdateOutletItemRequest request) {
        OutletItemDto outletItem = itemService.updateOutletItem(
                outletItemId,
                request.getQuantity() != null ? request.getQuantity() : 0,
                currentUser.getId()
        );
        return ResponseEntity.ok(ApiResponse.success(outletItem, "Outlet item updated"));
    }

    @DeleteMapping("/outlets/{outletId}/items/{outletItemId}")
    @Operation(summary = "Remove item from outlet")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<Void>> removeItemFromOutlet(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long outletId,
            @PathVariable Long outletItemId) {
        itemService.removeItemFromOutlet(outletItemId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Item removed from outlet"));
    }

    // =====================
    // Items List Endpoint
    // =====================

    @GetMapping("/items")
    @Operation(summary = "List items with filters (scoped by role)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ItemListResponseDto>>> getItems(
            ItemFilterDto filter,
            Pageable pageable) {
        Page<ItemListResponseDto> items = itemService.getAllItems(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(items)));
    }

    // Request DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddItemToOutletRequest {
        private Long itemId;
        private Integer quantity;
        private LocalDateTime pickupStartTime;
        private LocalDateTime pickupEndTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateOutletItemRequest {
        private Integer quantity;
        private LocalDateTime pickupStartTime;
        private LocalDateTime pickupEndTime;
        private BigDecimal discountedPrice;
    }
}
