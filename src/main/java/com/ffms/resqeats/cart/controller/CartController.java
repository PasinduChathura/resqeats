package com.ffms.resqeats.cart.controller;

import com.ffms.resqeats.cart.dto.CartDto;
import com.ffms.resqeats.cart.service.CartService;
import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Cart management APIs")
@PreAuthorize("hasRole('CUSTOMER_USER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get user's cart")
    public ResponseEntity<ApiResponse<CartDto>> getCart(@CurrentUser CustomUserDetails currentUser) {
        CartDto cart = cartService.getCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody AddToCartRequest request) {
        CartDto cart = cartService.addItem(
                currentUser.getId(),
                request.getOutletItemId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart"));
    }

    @PutMapping("/items/{outletItemId}")
    @Operation(summary = "Update item quantity")
    public ResponseEntity<ApiResponse<CartDto>> updateItemQuantity(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long outletItemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        CartDto cart = cartService.updateItemQuantity(
                currentUser.getId(),
                outletItemId,
                request.getQuantity()
        );
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart updated"));
    }

    @DeleteMapping("/items/{outletItemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long outletItemId) {
        CartDto cart = cartService.removeItem(currentUser.getId(), outletItemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart"));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@CurrentUser CustomUserDetails currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate cart before checkout")
    public ResponseEntity<ApiResponse<CartDto>> validateCart(@CurrentUser CustomUserDetails currentUser) {
        CartDto cart = cartService.validateCart(currentUser.getId());
        String message = cart.hasValidationIssues() 
                ? "Cart validated with adjustments" 
                : "Cart is valid";
        return ResponseEntity.ok(ApiResponse.success(cart, message));
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
