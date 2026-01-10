package com.ffms.resqeats.cart.service;

import com.ffms.resqeats.cart.dto.CartDto;
import com.ffms.resqeats.cart.dto.CartItemDto;
import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.inventory.service.InventoryService;
import com.ffms.resqeats.item.entity.Item;
import com.ffms.resqeats.item.entity.OutletItem;
import com.ffms.resqeats.item.repository.ItemRepository;
import com.ffms.resqeats.item.repository.OutletItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

/**
 * Service for managing shopping cart with Redis-based soft-state storage.
 *
 * <p>Design characteristics:</p>
 * <ul>
 *   <li>Cart is stored in Redis with 10-minute TTL</li>
 *   <li>Single-outlet restriction (clear cart when changing outlets)</li>
 *   <li>No DB persistence (soft-state design)</li>
 *   <li>Price locked when item added to cart</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final InventoryService inventoryService;
    private final OutletItemRepository outletItemRepository;
    private final ItemRepository itemRepository;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final Duration CART_TTL = Duration.ofMinutes(10);

    /**
     * Retrieves the current user's shopping cart.
     *
     * @param userId the user ID
     * @return the cart DTO or empty cart if none exists
     */
    @SuppressWarnings("unchecked")
    public CartDto getCart(Long userId) {
        log.info("Retrieving cart for userId: {}", userId);
        String cartKey = CART_KEY_PREFIX + userId;
        Map<String, Object> cartData = (Map<String, Object>) redisTemplate.opsForValue().get(cartKey);

        if (cartData == null) {
            log.debug("No cart found for userId: {}, returning empty cart", userId);
            return CartDto.empty(userId);
        }

        redisTemplate.expire(cartKey, CART_TTL);
        log.debug("Cart retrieved and TTL refreshed for userId: {}", userId);
        return mapToCartDto(cartData, userId);
    }

    /**
     * Adds an item to the user's cart.
     *
     * @param userId the user ID
     * @param outletItemId the outlet item ID to add
     * @param quantity the quantity to add
     * @return the updated cart DTO
     * @throws BusinessException if item not found, insufficient stock, or outlet mismatch
     */
    @SuppressWarnings("unchecked")
    public CartDto addItem(Long userId, Long outletItemId, int quantity) {
        log.info("Adding item to cart - userId: {}, outletItemId: {}, quantity: {}", userId, outletItemId, quantity);
        
        if (quantity <= 0) {
            log.warn("Invalid quantity provided: {}", quantity);
            throw new BusinessException("CART_001", "Quantity must be positive");
        }

        OutletItem outletItem = outletItemRepository.findById(outletItemId)
                .orElseThrow(() -> {
                    log.warn("Outlet item not found: {}", outletItemId);
                    return new BusinessException("CART_002", "Item not found");
                });

        Item item = itemRepository.findById(outletItem.getItemId())
                .orElseThrow(() -> {
                    log.warn("Item not found for outletItem: {}", outletItemId);
                    return new BusinessException("CART_002", "Item not found");
                });

        Long outletId = outletItem.getOutletId();

        int available = inventoryService.getAvailableStock(outletItemId);
        if (available < quantity) {
            log.warn("Insufficient stock - requested: {}, available: {}", quantity, available);
            throw new BusinessException("CART_003", "Insufficient stock. Available: " + available);
        }

        String cartKey = CART_KEY_PREFIX + userId;
        Map<String, Object> cartData = (Map<String, Object>) redisTemplate.opsForValue().get(cartKey);

        if (cartData == null) {
            cartData = new HashMap<>();
            cartData.put("outletId", outletId.toString());
            cartData.put("items", new HashMap<String, Map<String, Object>>());
            log.debug("Created new cart for userId: {}", userId);
        }

        String existingOutletId = (String) cartData.get("outletId");
        if (existingOutletId != null && !existingOutletId.equals(outletId.toString())) {
            log.warn("Cannot add items from different outlets - existing: {}, attempted: {}", existingOutletId, outletId);
            throw new BusinessException("CART_004", "Cannot add items from different outlets. Clear cart first.");
        }

        cartData.put("outletId", outletId.toString());

        Map<String, Map<String, Object>> items = (Map<String, Map<String, Object>>) cartData.get("items");
        if (items == null) {
            items = new HashMap<>();
            cartData.put("items", items);
        }

        Map<String, Object> cartItem = items.get(outletItemId.toString());
        if (cartItem == null) {
            cartItem = new HashMap<>();
            cartItem.put("outletItemId", outletItemId.toString());
            cartItem.put("itemId", item.getId().toString());
            cartItem.put("itemName", item.getName());
            cartItem.put("unitPrice", item.getSalePrice().toString());
            cartItem.put("originalPrice", item.getBasePrice().toString());
            cartItem.put("quantity", quantity);
            log.debug("Added new item to cart: {}", item.getName());
        } else {
            int existingQty = (int) cartItem.get("quantity");
            int newQty = existingQty + quantity;
            if (newQty > available) {
                log.warn("Cannot add more items - total would exceed available stock: {}", available);
                throw new BusinessException("CART_003", "Cannot add more. Available: " + available);
            }
            cartItem.put("quantity", newQty);
            log.debug("Updated item quantity in cart: {} -> {}", existingQty, newQty);
        }

        items.put(outletItemId.toString(), cartItem);

        String cartId = userId.toString();
        inventoryService.reserveForCart(outletItemId, quantity, cartId);
        log.debug("Stock reserved for cart: {}", cartId);

        redisTemplate.opsForValue().set(cartKey, cartData, CART_TTL);
        log.info("Item added to cart successfully - userId: {}, item: {}", userId, item.getName());
        return mapToCartDto(cartData, userId);
    }

    /**
     * Updates the quantity of an item in the cart.
     *
     * @param userId the user ID
     * @param outletItemId the outlet item ID to update
     * @param quantity the new quantity
     * @return the updated cart DTO
     * @throws BusinessException if cart empty, item not in cart, or insufficient stock
     */
    @SuppressWarnings("unchecked")
    public CartDto updateItemQuantity(Long userId, Long outletItemId, int quantity) {
        log.info("Updating cart item - userId: {}, outletItemId: {}, newQuantity: {}", userId, outletItemId, quantity);
        
        if (quantity < 0) {
            log.warn("Invalid quantity provided: {}", quantity);
            throw new BusinessException("CART_001", "Quantity cannot be negative");
        }

        if (quantity == 0) {
            log.debug("Quantity is 0, removing item from cart");
            return removeItem(userId, outletItemId);
        }

        String cartKey = CART_KEY_PREFIX + userId;
        Map<String, Object> cartData = (Map<String, Object>) redisTemplate.opsForValue().get(cartKey);

        if (cartData == null) {
            log.warn("Cart not found for userId: {}", userId);
            throw new BusinessException("CART_005", "Cart is empty");
        }

        Map<String, Map<String, Object>> items = (Map<String, Map<String, Object>>) cartData.get("items");
        Map<String, Object> cartItem = items.get(outletItemId.toString());

        if (cartItem == null) {
            log.warn("Item not found in cart: {}", outletItemId);
            throw new BusinessException("CART_006", "Item not in cart");
        }

        int available = inventoryService.getAvailableStock(outletItemId);
        if (available < quantity) {
            log.warn("Insufficient stock - requested: {}, available: {}", quantity, available);
            throw new BusinessException("CART_003", "Insufficient stock. Available: " + available);
        }

        int oldQty = (int) cartItem.get("quantity");
        int qtyDiff = quantity - oldQty;

        String cartId = userId.toString();
        if (qtyDiff > 0) {
            if (!inventoryService.reserveForCart(outletItemId, qtyDiff, cartId)) {
                log.warn("Failed to reserve additional stock for cart");
                throw new BusinessException("CART_003", "Cannot reserve additional stock");
            }
        } else if (qtyDiff < 0) {
            inventoryService.releaseCartReservation(outletItemId, cartId);
            inventoryService.reserveForCart(outletItemId, quantity, cartId);
        }

        cartItem.put("quantity", quantity);
        redisTemplate.opsForValue().set(cartKey, cartData, CART_TTL);

        log.info("Cart item quantity updated - userId: {}, outletItemId: {}, oldQty: {}, newQty: {}", 
                userId, outletItemId, oldQty, quantity);
        return mapToCartDto(cartData, userId);
    }

    /**
     * Removes an item from the cart.
     *
     * @param userId the user ID
     * @param outletItemId the outlet item ID to remove
     * @return the updated cart DTO
     */
    @SuppressWarnings("unchecked")
    public CartDto removeItem(Long userId, Long outletItemId) {
        log.info("Removing item from cart - userId: {}, outletItemId: {}", userId, outletItemId);
        String cartKey = CART_KEY_PREFIX + userId;
        Map<String, Object> cartData = (Map<String, Object>) redisTemplate.opsForValue().get(cartKey);

        if (cartData == null) {
            log.debug("Cart already empty for userId: {}", userId);
            return CartDto.empty(userId);
        }

        Map<String, Map<String, Object>> items = (Map<String, Map<String, Object>>) cartData.get("items");
        if (items != null) {
            items.remove(outletItemId.toString());
            String cartId = userId.toString();
            inventoryService.releaseCartReservation(outletItemId, cartId);
            log.debug("Released cart reservation for outletItemId: {}", outletItemId);
        }

        if (items == null || items.isEmpty()) {
            redisTemplate.delete(cartKey);
            log.info("Cart cleared (last item removed) for userId: {}", userId);
            return CartDto.empty(userId);
        }

        redisTemplate.opsForValue().set(cartKey, cartData, CART_TTL);
        log.info("Item removed from cart successfully - userId: {}, outletItemId: {}", userId, outletItemId);
        return mapToCartDto(cartData, userId);
    }

    /**
     * Clears the entire cart for a user.
     *
     * @param userId the user ID
     */
    @SuppressWarnings("unchecked")
    public void clearCart(Long userId) {
        log.info("Clearing cart for userId: {}", userId);
        String cartKey = CART_KEY_PREFIX + userId;
        Map<String, Object> cartData = (Map<String, Object>) redisTemplate.opsForValue().get(cartKey);

        if (cartData != null) {
            Map<String, Map<String, Object>> items = (Map<String, Map<String, Object>>) cartData.get("items");
            if (items != null) {
                String cartId = userId.toString();
                items.keySet().forEach(outletItemId -> {
                    inventoryService.releaseCartReservation(Long.valueOf(outletItemId), cartId);
                    log.debug("Released cart reservation for outletItemId: {}", outletItemId);
                });
            }
        }

        redisTemplate.delete(cartKey);
        log.info("Cart cleared successfully for userId: {}", userId);
    }

    /**
     * Validates cart before checkout, removing out-of-stock items and adjusting quantities.
     *
     * @param userId the user ID
     * @return the validated cart DTO with removed/adjusted item lists
     * @throws BusinessException if cart is empty or all items out of stock
     */
    @SuppressWarnings("unchecked")
    public CartDto validateCart(Long userId) {
        log.info("Validating cart for checkout - userId: {}", userId);
        String cartKey = CART_KEY_PREFIX + userId;
        Map<String, Object> cartData = (Map<String, Object>) redisTemplate.opsForValue().get(cartKey);

        if (cartData == null) {
            log.warn("Cart validation failed - cart is empty for userId: {}", userId);
            throw new BusinessException("CART_005", "Cart is empty");
        }

        Map<String, Map<String, Object>> items = (Map<String, Map<String, Object>>) cartData.get("items");
        if (items == null || items.isEmpty()) {
            log.warn("Cart validation failed - no items in cart for userId: {}", userId);
            throw new BusinessException("CART_005", "Cart is empty");
        }

        List<String> removedItems = new ArrayList<>();
        List<String> adjustedItems = new ArrayList<>();

        List<Long> outletItemIds = items.keySet().stream()
            .map(Long::valueOf)
            .collect(java.util.stream.Collectors.toList());
        Map<Long, Integer> stockLevels = inventoryService.getStockLevels(outletItemIds);
        log.debug("Batch fetched stock levels for {} items", outletItemIds.size());

        Iterator<Map.Entry<String, Map<String, Object>>> iterator = items.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Map<String, Object>> entry = iterator.next();
            Long outletItemId = Long.valueOf(entry.getKey());
            Map<String, Object> cartItem = entry.getValue();

            int requestedQty = (int) cartItem.get("quantity");
            int available = stockLevels.getOrDefault(outletItemId, 0);

            if (available == 0) {
                iterator.remove();
                String itemName = (String) cartItem.get("itemName");
                removedItems.add(itemName);
                log.warn("Item removed from cart (out of stock): {}", itemName);
            } else if (available < requestedQty) {
                cartItem.put("quantity", available);
                String itemName = (String) cartItem.get("itemName");
                adjustedItems.add(itemName + " (adjusted to " + available + ")");
                log.warn("Item quantity adjusted in cart: {} from {} to {}", itemName, requestedQty, available);
            }
        }

        if (items.isEmpty()) {
            redisTemplate.delete(cartKey);
            log.error("Cart validation failed - all items out of stock for userId: {}", userId);
            throw new BusinessException("CART_007", "All items are out of stock");
        }

        redisTemplate.opsForValue().set(cartKey, cartData, CART_TTL);

        CartDto cart = mapToCartDto(cartData, userId);
        cart.setRemovedItems(removedItems);
        cart.setAdjustedItems(adjustedItems);

        log.info("Cart validated successfully - userId: {}, removed: {}, adjusted: {}", 
                userId, removedItems.size(), adjustedItems.size());
        return cart;
    }

    /**
     * Retrieves and validates cart for checkout process.
     *
     * @param userId the user ID
     * @return the validated cart DTO ready for checkout
     */
    public CartDto getCartForCheckout(Long userId) {
        log.info("Getting cart for checkout - userId: {}", userId);
        CartDto validatedCart = validateCart(userId);
        log.info("Cart ready for checkout - userId: {}, itemCount: {}", userId, validatedCart.getItemCount());
        return validatedCart;
    }

    /**
     * Maps cart data from Redis to CartDto.
     *
     * @param cartData the raw cart data map
     * @param userId the user ID
     * @return the mapped CartDto
     */
    @SuppressWarnings("unchecked")
    private CartDto mapToCartDto(Map<String, Object> cartData, Long userId) {
        CartDto cart = new CartDto();
        cart.setUserId(userId);
        cart.setOutletId(cartData.get("outletId") != null 
                ? Long.valueOf((String) cartData.get("outletId")) : null);

        Map<String, Map<String, Object>> items = (Map<String, Map<String, Object>>) cartData.get("items");
        List<CartItemDto> cartItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalSavings = BigDecimal.ZERO;

        if (items != null) {
            for (Map<String, Object> itemData : items.values()) {
                CartItemDto item = new CartItemDto();
                item.setOutletItemId(Long.valueOf((String) itemData.get("outletItemId")));
                item.setItemId(Long.valueOf((String) itemData.get("itemId")));
                item.setItemName((String) itemData.get("itemName"));
                item.setUnitPrice(new BigDecimal((String) itemData.get("unitPrice")));
                item.setOriginalPrice(new BigDecimal((String) itemData.get("originalPrice")));
                item.setQuantity((int) itemData.get("quantity"));

                BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal lineSavings = item.getOriginalPrice()
                        .subtract(item.getUnitPrice())
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                item.setLineTotal(lineTotal);
                item.setLineSavings(lineSavings);

                cartItems.add(item);
                subtotal = subtotal.add(lineTotal);
                totalSavings = totalSavings.add(lineSavings);
            }
        }

        cart.setItems(cartItems);
        cart.setSubtotal(subtotal);
        cart.setTotalSavings(totalSavings);
        cart.setItemCount(cartItems.size());
        cart.setTotalQuantity(cartItems.stream().mapToInt(CartItemDto::getQuantity).sum());

        return cart;
    }
}
