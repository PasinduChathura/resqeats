package com.ffms.resqeats.cart.controller;

import com.ffms.resqeats.cart.dto.CartDto;
import com.ffms.resqeats.cart.service.CartService;
import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Cart controller per SRS Section 6.2.
 *
 * Endpoints:
 * GET /cart - Get user's cart
 * POST /cart/items - Add item to cart
 * PUT /cart/items/{outletItemId} - Update item quantity
 * DELETE /cart/items/{outletItemId} - Remove item from cart
 * DELETE /cart - Clear cart
 * POST /cart/validate - Validate cart before checkout
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Cart management APIs")
@PreAuthorize("hasRole('CUSTOMER_USER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get user's cart")
    public ResponseEntity<ApiResponse<CartDto>> getCart(@CurrentUser UserPrincipal currentUser) {
        log.info("Get cart request for userId: {}", currentUser.getId());
        try {
            CartDto cart = cartService.getCart(currentUser.getId());
            log.info("Cart retrieved successfully for userId: {} - items: {}", currentUser.getId(), 
                    cart.getItems() != null ? cart.getItems().size() : 0);
            return ResponseEntity.ok(ApiResponse.success(cart));
        } catch (Exception e) {
            log.error("Failed to get cart for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Add to cart request for userId: {} - outletItemId: {}, quantity: {}", 
                currentUser.getId(), request.getOutletItemId(), request.getQuantity());
        try {
            CartDto cart = cartService.addItem(
                    currentUser.getId(),
                    request.getOutletItemId(),
                    request.getQuantity()
            );
            log.info("Item added to cart successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart"));
        } catch (Exception e) {
            log.error("Failed to add item to cart for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/items/{outletItemId}")
    @Operation(summary = "Update item quantity")
    public ResponseEntity<ApiResponse<CartDto>> updateItemQuantity(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long outletItemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        log.info("Update cart item quantity for userId: {} - outletItemId: {}, quantity: {}", 
                currentUser.getId(), outletItemId, request.getQuantity());
        try {
            CartDto cart = cartService.updateItemQuantity(
                    currentUser.getId(),
                    outletItemId,
                    request.getQuantity()
            );
            log.info("Cart item quantity updated successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(cart, "Cart updated"));
        } catch (Exception e) {
            log.error("Failed to update cart item for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/items/{outletItemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long outletItemId) {
        log.info("Remove item from cart for userId: {} - outletItemId: {}", currentUser.getId(), outletItemId);
        try {
            CartDto cart = cartService.removeItem(currentUser.getId(), outletItemId);
            log.info("Item removed from cart successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart"));
        } catch (Exception e) {
            log.error("Failed to remove item from cart for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@CurrentUser UserPrincipal currentUser) {
        log.info("Clear cart request for userId: {}", currentUser.getId());
        try {
            cartService.clearCart(currentUser.getId());
            log.info("Cart cleared successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
        } catch (Exception e) {
            log.error("Failed to clear cart for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate cart before checkout")
    public ResponseEntity<ApiResponse<CartDto>> validateCart(@CurrentUser UserPrincipal currentUser) {
        log.info("Validate cart request for userId: {}", currentUser.getId());
        try {
            CartDto cart = cartService.validateCart(currentUser.getId());
            String message = cart.hasValidationIssues() 
                    ? "Cart validated with adjustments" 
                    : "Cart is valid";
            log.info("Cart validation completed for userId: {} - hasIssues: {}", 
                    currentUser.getId(), cart.hasValidationIssues());
            return ResponseEntity.ok(ApiResponse.success(cart, message));
        } catch (Exception e) {
            log.error("Failed to validate cart for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    // Request DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddToCartRequest {
        @NotNull(message = "Outlet item ID is required")
        private Long outletItemId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateQuantityRequest {
        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}
