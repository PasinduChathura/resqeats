package com.ffms.resqeats.controller.cart;

import com.ffms.resqeats.dto.cart.*;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse response = cartService.getOrCreateCart(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse response = cartService.addToCart(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{secretBoxId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Long secretBoxId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse response = cartService.updateCartItemQuantity(userDetails.getId(), secretBoxId, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{secretBoxId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long secretBoxId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse response = cartService.removeFromCart(userDetails.getId(), secretBoxId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> validateCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isValid = cartService.validateCartAvailability(userDetails.getId());
        return ResponseEntity.ok(isValid);
    }
}
