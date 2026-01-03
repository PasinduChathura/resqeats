package com.ffms.resqeats.service.cart.impl;

import com.ffms.resqeats.common.logging.AppLogger;
import com.ffms.resqeats.dto.cart.*;
import com.ffms.resqeats.enums.cart.CartStatus;
import com.ffms.resqeats.exception.cart.CartException;
import com.ffms.resqeats.models.cart.Cart;
import com.ffms.resqeats.models.cart.CartItem;
import com.ffms.resqeats.models.food.SecretBox;
import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.repository.cart.CartItemRepository;
import com.ffms.resqeats.repository.cart.CartRepository;
import com.ffms.resqeats.repository.food.SecretBoxRepository;
import com.ffms.resqeats.repository.usermgt.UserRepository;
import com.ffms.resqeats.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final AppLogger appLogger = AppLogger.of(log);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final SecretBoxRepository secretBoxRepository;
    private final UserRepository userRepository;

    @Value("${resqeats.cart.expiry-minutes:10}")
    private int cartExpiryMinutes;

    @Override
    public CartResponse getOrCreateCart(Long userId) {
        appLogger.debug("Getting or creating cart for user: {}", userId);
        
        Optional<Cart> existingCart = cartRepository.findActiveCartWithItemsByUserId(userId);
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            if (cart.isExpired()) {
                appLogger.info("Cart expired for user {}, creating new cart", userId);
                cart.setStatus(CartStatus.EXPIRED);
                cartRepository.save(cart);
                return createNewCart(userId);
            }
            return mapToCartResponse(cart);
        }
        
        return createNewCart(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        appLogger.debug("Fetching cart for user: {}", userId);
        
        Cart cart = cartRepository.findActiveCartWithItemsByUserId(userId)
                .orElseThrow(() -> {
                    appLogger.logWarning("READ", "Cart", userId, "No active cart found");
                    return CartException.noActiveCart();
                });
        
        if (cart.isExpired()) {
            appLogger.logWarning("READ", "Cart", cart.getId(), "Cart has expired");
            throw CartException.expired();
        }
        
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        appLogger.logStart("ADD_ITEM", "Cart", userId);
        
        Cart cart = getOrCreateCartEntity(userId);
        
        SecretBox secretBox = secretBoxRepository.findById(request.getSecretBoxId())
                .orElseThrow(() -> {
                    appLogger.logWarning("ADD_ITEM", "Cart", userId, 
                            "Secret box not found: " + request.getSecretBoxId());
                    return CartException.secretBoxNotFound(request.getSecretBoxId());
                });
        
        if (!secretBox.isAvailable()) {
            appLogger.logWarning("ADD_ITEM", "Cart", userId, 
                    "Secret box not available: " + secretBox.getName());
            throw CartException.itemUnavailable(secretBox.getName());
        }
        
        if (secretBox.getQuantityAvailable() < request.getQuantity()) {
            appLogger.logWarning("ADD_ITEM", "Cart", userId, 
                    String.format("Insufficient quantity: %s (requested: %d, available: %d)", 
                            secretBox.getName(), request.getQuantity(), secretBox.getQuantityAvailable()));
            throw CartException.insufficientQuantity(secretBox.getName(), secretBox.getQuantityAvailable());
        }
        
        // Check if item already in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getSecretBox().getId().equals(request.getSecretBoxId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > secretBox.getQuantityAvailable()) {
                appLogger.logWarning("ADD_ITEM", "Cart", userId, 
                        String.format("Cannot add more: %s (total: %d, available: %d)", 
                                secretBox.getName(), newQuantity, secretBox.getQuantityAvailable()));
                throw CartException.quantityExceeded(secretBox.getName(), secretBox.getQuantityAvailable());
            }
            item.setQuantity(newQuantity);
            item.calculateTotalPrice();
            appLogger.info("Updated cart item quantity: {} -> {}", item.getSecretBox().getName(), newQuantity);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .secretBox(secretBox)
                    .shop(secretBox.getShop())
                    .quantity(request.getQuantity())
                    .unitPrice(secretBox.getDiscountedPrice())
                    .build();
            cartItem.calculateTotalPrice();
            cart.getItems().add(cartItem);
        }
        
        cart.recalculateTotals();
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(cartExpiryMinutes));
        cart = cartRepository.save(cart);
        
        log.info("Item added to cart for user: {}", userId);
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItemQuantity(Long userId, Long secretBoxId, Integer quantity) {
        Cart cart = cartRepository.findActiveCartWithItemsByUserId(userId)
                .orElseThrow(() -> new CartException("No active cart found"));
        
        if (cart.isExpired()) {
            throw new CartException("Cart has expired");
        }
        
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getSecretBox().getId().equals(secretBoxId))
                .findFirst()
                .orElseThrow(() -> new CartException("Item not found in cart"));
        
        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            SecretBox secretBox = item.getSecretBox();
            if (quantity > secretBox.getQuantityAvailable()) {
                throw new CartException("Insufficient quantity available. Only " + secretBox.getQuantityAvailable() + " left");
            }
            item.setQuantity(quantity);
            item.calculateTotalPrice();
        }
        
        cart.recalculateTotals();
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(cartExpiryMinutes));
        cart = cartRepository.save(cart);
        
        log.info("Cart item quantity updated for user: {}", userId);
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse removeFromCart(Long userId, Long secretBoxId) {
        Cart cart = cartRepository.findActiveCartWithItemsByUserId(userId)
                .orElseThrow(() -> new CartException("No active cart found"));
        
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getSecretBox().getId().equals(secretBoxId))
                .findFirst()
                .orElseThrow(() -> new CartException("Item not found in cart"));
        
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        
        cart.recalculateTotals();
        cart = cartRepository.save(cart);
        
        log.info("Item removed from cart for user: {}", userId);
        return mapToCartResponse(cart);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findActiveCartWithItemsByUserId(userId)
                .orElseThrow(() -> new CartException("No active cart found"));
        
        cart.getItems().clear();
        cart.setTotalAmount(java.math.BigDecimal.ZERO);
        cart.setTotalItems(0);
        cartRepository.save(cart);
        
        log.info("Cart cleared for user: {}", userId);
    }

    @Override
    public void expireCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartException("Cart not found"));
        
        cart.setStatus(CartStatus.EXPIRED);
        cartRepository.save(cart);
        
        log.info("Cart expired: {}", cartId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCartAvailability(Long userId) {
        Cart cart = cartRepository.findActiveCartWithItemsByUserId(userId)
                .orElseThrow(() -> new CartException("No active cart found"));
        
        if (cart.isExpired()) {
            return false;
        }
        
        for (CartItem item : cart.getItems()) {
            SecretBox secretBox = secretBoxRepository.findById(item.getSecretBox().getId())
                    .orElse(null);
            if (secretBox == null || !secretBox.isAvailable() || 
                    secretBox.getQuantityAvailable() < item.getQuantity()) {
                return false;
            }
        }
        
        return true;
    }

    private Cart getOrCreateCartEntity(Long userId) {
        Optional<Cart> existingCart = cartRepository.findActiveCartByUserId(userId);
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            if (cart.isExpired()) {
                cart.setStatus(CartStatus.EXPIRED);
                cartRepository.save(cart);
                return createNewCartEntity(userId);
            }
            return cart;
        }
        
        return createNewCartEntity(userId);
    }

    private Cart createNewCartEntity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CartException("User not found"));
        
        Cart cart = Cart.builder()
                .user(user)
                .status(CartStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(cartExpiryMinutes))
                .build();
        
        return cartRepository.save(cart);
    }

    private CartResponse createNewCart(Long userId) {
        Cart cart = createNewCartEntity(userId);
        return mapToCartResponse(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = new ArrayList<>();
        
        if (cart.getItems() != null) {
            itemResponses = cart.getItems().stream()
                    .map(this::mapToCartItemResponse)
                    .collect(Collectors.toList());
        }
        
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .status(cart.getStatus())
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .expiresAt(cart.getExpiresAt())
                .items(itemResponses)
                .isExpired(cart.isExpired())
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .secretBoxId(item.getSecretBox().getId())
                .secretBoxName(item.getSecretBox().getName())
                .secretBoxImageUrl(item.getSecretBox().getImageUrl())
                .shopId(item.getShop().getId())
                .shopName(item.getShop().getName())
                .shopAddress(item.getShop().getAddress())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .quantityAvailable(item.getSecretBox().getQuantityAvailable())
                .pickupStartTime(item.getSecretBox().getPickupStartTime())
                .pickupEndTime(item.getSecretBox().getPickupEndTime())
                .build();
    }
}
