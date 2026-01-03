package com.ffms.resqeats.service.cart;

import com.ffms.resqeats.dto.cart.*;

public interface CartService {

    CartResponse getOrCreateCart(Long userId);

    CartResponse getCart(Long userId);

    CartResponse addToCart(Long userId, AddToCartRequest request);

    CartResponse updateCartItemQuantity(Long userId, Long secretBoxId, Integer quantity);

    CartResponse removeFromCart(Long userId, Long secretBoxId);

    void clearCart(Long userId);

    void expireCart(Long cartId);

    boolean validateCartAvailability(Long userId);
}
