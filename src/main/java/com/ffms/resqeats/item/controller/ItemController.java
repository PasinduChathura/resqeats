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
@RequestMapping("/api/v1")
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
        log.info("Get outlet items request for outletId: {}", outletId);
        try {
            List<OutletItemDto> items = itemService.getAvailableOutletItems(outletId);
            log.info("Retrieved {} items for outlet: {}", items.size(), outletId);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("Failed to get items for outlet: {} - Error: {}", outletId, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/items/search")
    @Operation(summary = "Search items")
    public ResponseEntity<ApiResponse<PageResponse<OutletItemDto>>> searchItems(
            @RequestParam String query,
            Pageable pageable) {
        log.info("Search items request - query: {}, page: {}", query, pageable.getPageNumber());
        try {
            Page<OutletItemDto> items = itemService.searchItems(query, pageable);
            log.info("Found {} items for query: {}", items.getTotalElements(), query);
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(items)));
        } catch (Exception e) {
            log.error("Failed to search items - Error: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get item details")
    public ResponseEntity<ApiResponse<ItemDto>> getItem(@PathVariable Long id) {
        log.info("Get item details request for itemId: {}", id);
        try {
            ItemDto item = itemService.getItem(id);
            log.info("Successfully retrieved item: {}", id);
            return ResponseEntity.ok(ApiResponse.success(item));
        } catch (Exception e) {
            log.error("Failed to get item: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // =====================
    // Merchant Endpoints
    // =====================

    @PostMapping("/merchants/{merchantId}/items")
    @Operation(summary = "Create item")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<ItemDto>> createItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long merchantId,
            @Valid @RequestBody CreateItemRequest request) {
        log.info("Create item request for merchantId: {} - Name: {}", merchantId, request.getName());
        try {
            ItemDto item = itemService.createItem(merchantId, request, currentUser.getId());
            log.info("Item created successfully: {} for merchant: {}", item.getId(), merchantId);
            return ResponseEntity.ok(ApiResponse.success(item, "Item created"));
        } catch (Exception e) {
            log.error("Failed to create item for merchant: {} - Error: {}", merchantId, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/merchants/{merchantId}/items")
    @Operation(summary = "List merchant items")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<PageResponse<ItemDto>>> getMerchantItems(
            @PathVariable Long merchantId,
            Pageable pageable) {
        log.info("List items request for merchantId: {}, page: {}", merchantId, pageable.getPageNumber());
        try {
            Page<ItemDto> items = itemService.getMerchantItems(merchantId, pageable);
            log.info("Retrieved {} items for merchant: {}", items.getTotalElements(), merchantId);
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(items)));
        } catch (Exception e) {
            log.error("Failed to list items for merchant: {} - Error: {}", merchantId, e.getMessage());
            throw e;
        }
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update item")
    @PreAuthorize("hasRole('MERCHANT_USER')")
    public ResponseEntity<ApiResponse<ItemDto>> updateItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        log.info("Update item request for itemId: {} by userId: {}", id, currentUser.getId());
        try {
            ItemDto item = itemService.updateItem(id, request, currentUser.getId());
            log.info("Item updated successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(item, "Item updated"));
        } catch (Exception e) {
            log.error("Failed to update item: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // =====================
    // Outlet Item Endpoints
    // =====================

    @PostMapping("/outlets/{outletId}/items")
    @Operation(summary = "Add item to outlet")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletItemDto>> addItemToOutlet(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long outletId,
            @Valid @RequestBody AddItemToOutletRequest request) {
        log.info("Add item to outlet request - outletId: {}, itemId: {}", outletId, request.getItemId());
        try {
            OutletItemDto outletItem = itemService.addItemToOutlet(
                    outletId,
                    request.getItemId(),
                    request.getQuantity() != null ? request.getQuantity() : 0,
                    currentUser.getId()
            );
            log.info("Item added to outlet successfully: {} - Item: {}", outletId, request.getItemId());
            return ResponseEntity.ok(ApiResponse.success(outletItem, "Item added to outlet"));
        } catch (Exception e) {
            log.error("Failed to add item to outlet: {} - Error: {}", outletId, e.getMessage());
            throw e;
        }
    }

    @PutMapping("/outlets/{outletId}/items/{outletItemId}")
    @Operation(summary = "Update outlet item")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<OutletItemDto>> updateOutletItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long outletId,
            @PathVariable Long outletItemId,
            @Valid @RequestBody UpdateOutletItemRequest request) {
        log.info("Update outlet item request - outletId: {}, outletItemId: {}", outletId, outletItemId);
        try {
            OutletItemDto outletItem = itemService.updateOutletItem(
                    outletItemId,
                    request.getQuantity() != null ? request.getQuantity() : 0,
                    currentUser.getId()
            );
            log.info("Outlet item updated successfully: {}", outletItemId);
            return ResponseEntity.ok(ApiResponse.success(outletItem, "Outlet item updated"));
        } catch (Exception e) {
            log.error("Failed to update outlet item: {} - Error: {}", outletItemId, e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/outlets/{outletId}/items/{outletItemId}")
    @Operation(summary = "Remove item from outlet")
    @PreAuthorize("hasAnyRole('MERCHANT_USER', 'OUTLET_USER')")
    public ResponseEntity<ApiResponse<Void>> removeItemFromOutlet(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long outletId,
            @PathVariable Long outletItemId) {
        log.info("Remove item from outlet request - outletId: {}, outletItemId: {}", outletId, outletItemId);
        try {
            itemService.removeItemFromOutlet(outletItemId, currentUser.getId());
            log.info("Item removed from outlet successfully: {}", outletItemId);
            return ResponseEntity.ok(ApiResponse.success(null, "Item removed from outlet"));
        } catch (Exception e) {
            log.error("Failed to remove item from outlet: {} - Error: {}", outletItemId, e.getMessage());
            throw e;
        }
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
        log.info("List items request - filter: {}, page: {}", filter, pageable.getPageNumber());
        try {
            Page<ItemListResponseDto> items = itemService.getAllItems(filter, pageable);
            log.info("Retrieved {} items", items.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(items)));
        } catch (Exception e) {
            log.error("Failed to list items - Error: {}", e.getMessage());
            throw e;
        }
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
