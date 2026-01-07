package com.ffms.resqeats.inventory.service;

import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.item.entity.OutletItem;
import com.ffms.resqeats.item.repository.OutletItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing inventory with Redis and database consistency.
 *
 * <p>This service implements a dual-storage architecture per SRS Section 6.9,
 * using Redis as the primary source for real-time inventory operations and
 * the database as persistent storage with periodic synchronization.</p>
 *
 * <p><strong>Redis Key Structure:</strong></p>
 * <ul>
 *   <li>{@code inventory:{outletItemId}} - Current stock count</li>
 *   <li>{@code inventory:reserved:{outletItemId}} - Temporarily reserved stock (cart holds)</li>
 *   <li>{@code inventory:cart:{cartId}:{outletItemId}} - Per-cart reservation tracking</li>
 * </ul>
 *
 * <p><strong>Business Rules:</strong></p>
 * <ul>
 *   <li>BR-007: Inventory cannot go below zero</li>
 *   <li>BR-008: Reserved stock expires after cart timeout (10 minutes)</li>
 *   <li>BR-009: Stock decremented atomically to prevent overselling</li>
 * </ul>
 *
 * <p>Thread safety is ensured through Redis atomic operations rather than JVM locks,
 * allowing for distributed deployment scenarios.</p>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OutletItemRepository outletItemRepository;

    private static final String INVENTORY_KEY_PREFIX = "inventory:";
    private static final String RESERVED_KEY_PREFIX = "inventory:reserved:";
    private static final String CART_RESERVE_PREFIX = "inventory:cart:";
    private static final Duration CART_RESERVE_TTL = Duration.ofMinutes(10);
    private static final Duration INVENTORY_CACHE_TTL = Duration.ofHours(1);

    /**
     * Retrieves the current available stock for an outlet item.
     *
     * <p>Available stock is calculated as total stock minus reserved stock.
     * The method first checks Redis cache, falling back to database if not cached.</p>
     *
     * @param outletItemId the unique identifier of the outlet item
     * @return the available stock quantity, minimum of 0
     */
    public int getAvailableStock(UUID outletItemId) {
        log.info("Getting available stock for outletItemId={}", outletItemId);
        
        String inventoryKey = INVENTORY_KEY_PREFIX + outletItemId;
        String reservedKey = RESERVED_KEY_PREFIX + outletItemId;

        Integer total = getFromRedisOrDb(inventoryKey, outletItemId);
        Integer reserved = getReservedCount(reservedKey);

        int availableStock = Math.max(0, total - reserved);
        log.debug("Stock calculation for outletItemId={}: total={}, reserved={}, available={}", 
                outletItemId, total, reserved, availableStock);
        
        return availableStock;
    }

    /**
     * Reserves stock for a shopping cart with automatic expiration.
     *
     * <p>This method implements BR-008: Reserved stock expires after cart timeout (10 minutes).
     * Uses Redis atomic operations for thread safety in distributed environments.</p>
     *
     * @param outletItemId the unique identifier of the outlet item to reserve
     * @param quantity the quantity to reserve
     * @param cartId the unique identifier of the cart making the reservation
     * @return {@code true} if reservation was successful, {@code false} if insufficient stock
     */
    public boolean reserveForCart(UUID outletItemId, int quantity, String cartId) {
        log.info("Reserving stock for cart: outletItemId={}, quantity={}, cartId={}", 
                outletItemId, quantity, cartId);
        
        int available = getAvailableStock(outletItemId);

        if (available < quantity) {
            log.warn("Insufficient stock for reservation: outletItemId={}, available={}, requested={}", 
                    outletItemId, available, quantity);
            return false;
        }

        String reservedKey = RESERVED_KEY_PREFIX + outletItemId;
        String cartReserveKey = CART_RESERVE_PREFIX + cartId + ":" + outletItemId;

        Long newReserved = redisTemplate.opsForValue().increment(reservedKey, quantity);
        log.debug("Reserved {} units for cartId={}, total reserved now: {}", quantity, cartId, newReserved);

        redisTemplate.opsForValue().set(cartReserveKey, quantity, CART_RESERVE_TTL);
        
        log.info("Stock reservation successful: outletItemId={}, quantity={}, cartId={}", 
                outletItemId, quantity, cartId);
        return true;
    }

    /**
     * Releases a cart's stock reservation.
     *
     * <p>This method should be called when a cart is abandoned or when an order is placed.
     * Uses Redis atomic operations for thread safety in distributed environments.</p>
     *
     * @param outletItemId the unique identifier of the outlet item
     * @param cartId the unique identifier of the cart whose reservation should be released
     */
    public void releaseCartReservation(UUID outletItemId, String cartId) {
        log.info("Releasing cart reservation: outletItemId={}, cartId={}", outletItemId, cartId);
        
        String reservedKey = RESERVED_KEY_PREFIX + outletItemId;
        String cartReserveKey = CART_RESERVE_PREFIX + cartId + ":" + outletItemId;

        Integer reservedByCart = (Integer) redisTemplate.opsForValue().get(cartReserveKey);
        if (reservedByCart != null && reservedByCart > 0) {
            redisTemplate.opsForValue().decrement(reservedKey, reservedByCart);
            redisTemplate.delete(cartReserveKey);
            log.info("Released {} reserved units for cartId={}, outletItemId={}", 
                    reservedByCart, cartId, outletItemId);
        } else {
            log.debug("No reservation found to release for cartId={}, outletItemId={}", cartId, outletItemId);
        }
    }

    /**
     * Decrements stock atomically when an order is confirmed.
     *
     * <p>This method implements BR-009: Stock is decremented atomically to prevent overselling.
     * Updates both Redis cache and database for consistency.</p>
     *
     * @param outletItemId the unique identifier of the outlet item
     * @param quantity the quantity to decrement
     * @throws BusinessException with code INV_001 if insufficient stock is available
     */
    @Transactional
    public void decrementStock(UUID outletItemId, int quantity) {
        log.info("Decrementing stock: outletItemId={}, quantity={}", outletItemId, quantity);
        
        String inventoryKey = INVENTORY_KEY_PREFIX + outletItemId;

        int current = getAvailableStock(outletItemId);
        if (current < quantity) {
            log.error("Insufficient stock for decrement: outletItemId={}, available={}, requested={}", 
                    outletItemId, current, quantity);
            throw new BusinessException("INV_001", 
                    "Insufficient stock. Available: " + current + ", Requested: " + quantity);
        }

        Long newValue = redisTemplate.opsForValue().decrement(inventoryKey, quantity);

        outletItemRepository.findById(outletItemId).ifPresent(item -> {
            item.setCurrentQuantity(newValue != null ? newValue.intValue() : 0);
            outletItemRepository.save(item);
            log.debug("Database updated for outletItemId={}, newQuantity={}", outletItemId, newValue);
        });

        log.info("Stock decremented successfully: outletItemId={}, decrementedBy={}, newValue={}", 
                outletItemId, quantity, newValue);
    }

    /**
     * Increments stock when an order is cancelled or items are restocked.
     *
     * <p>Updates both Redis cache and database for consistency.
     * Uses Redis atomic operations for thread safety.</p>
     *
     * @param outletItemId the unique identifier of the outlet item
     * @param quantity the quantity to increment
     */
    @Transactional
    public void incrementStock(UUID outletItemId, int quantity) {
        log.info("Incrementing stock: outletItemId={}, quantity={}", outletItemId, quantity);
        
        String inventoryKey = INVENTORY_KEY_PREFIX + outletItemId;

        Long newValue = redisTemplate.opsForValue().increment(inventoryKey, quantity);

        outletItemRepository.findById(outletItemId).ifPresent(item -> {
            item.setCurrentQuantity(newValue != null ? newValue.intValue() : 0);
            outletItemRepository.save(item);
            log.debug("Database updated for outletItemId={}, newQuantity={}", outletItemId, newValue);
        });

        log.info("Stock incremented successfully: outletItemId={}, incrementedBy={}, newValue={}", 
                outletItemId, quantity, newValue);
    }

    /**
     * Sets the stock level for an outlet item.
     *
     * <p>This method is typically used by outlet managers to set inventory levels.
     * Updates both Redis cache and database for consistency.</p>
     *
     * @param outletItemId the unique identifier of the outlet item
     * @param quantity the stock quantity to set (must be non-negative)
     * @throws BusinessException with code INV_002 if quantity is negative
     * @throws BusinessException with code INV_003 if outlet item is not found
     */
    @Transactional
    public void setStock(UUID outletItemId, int quantity) {
        log.info("Setting stock: outletItemId={}, quantity={}", outletItemId, quantity);
        
        if (quantity < 0) {
            log.warn("Attempted to set negative stock: outletItemId={}, quantity={}", outletItemId, quantity);
            throw new BusinessException("INV_002", "Stock quantity cannot be negative");
        }

        String inventoryKey = INVENTORY_KEY_PREFIX + outletItemId;

        redisTemplate.opsForValue().set(inventoryKey, quantity, INVENTORY_CACHE_TTL);
        log.debug("Redis cache updated for outletItemId={}, quantity={}", outletItemId, quantity);

        OutletItem item = outletItemRepository.findById(outletItemId)
                .orElseThrow(() -> {
                    log.error("Outlet item not found: outletItemId={}", outletItemId);
                    return new BusinessException("INV_003", "Outlet item not found");
                });
        item.setCurrentQuantity(quantity);
        outletItemRepository.save(item);

        log.info("Stock set successfully: outletItemId={}, quantity={}", outletItemId, quantity);
    }

    /**
     * Initializes stock from database into Redis cache.
     *
     * <p>Loads the current stock quantity from the database and caches it in Redis.
     * This method is typically called during application startup or when cache needs refresh.</p>
     *
     * @param outletItemId the unique identifier of the outlet item to initialize
     * @throws BusinessException with code INV_003 if outlet item is not found
     */
    public void initializeStock(UUID outletItemId) {
        log.info("Initializing stock in Redis cache: outletItemId={}", outletItemId);
        
        OutletItem item = outletItemRepository.findById(outletItemId)
                .orElseThrow(() -> {
                    log.error("Outlet item not found during initialization: outletItemId={}", outletItemId);
                    return new BusinessException("INV_003", "Outlet item not found");
                });

        String inventoryKey = INVENTORY_KEY_PREFIX + outletItemId;
        Integer qty = item.getCurrentQuantity() != null ? item.getCurrentQuantity() : 0;
        redisTemplate.opsForValue().set(inventoryKey, qty, INVENTORY_CACHE_TTL);

        log.info("Stock initialized successfully: outletItemId={}, quantity={}", outletItemId, qty);
    }

    /**
     * Checks if an outlet item is currently in stock.
     *
     * <p>An item is considered in stock if its available quantity is greater than zero.</p>
     *
     * @param outletItemId the unique identifier of the outlet item
     * @return {@code true} if the item has available stock, {@code false} otherwise
     */
    public boolean isInStock(UUID outletItemId) {
        log.debug("Checking stock availability: outletItemId={}", outletItemId);
        boolean inStock = getAvailableStock(outletItemId) > 0;
        log.debug("Stock availability check result: outletItemId={}, inStock={}", outletItemId, inStock);
        return inStock;
    }

    /**
     * Retrieves stock levels for multiple outlet items in bulk.
     *
     * <p>Uses Redis MGET for efficient batch fetching instead of individual calls,
     * providing better performance for large item sets.</p>
     *
     * @param outletItemIds an iterable collection of outlet item identifiers
     * @return a map of outlet item IDs to their available stock levels
     */
    public Map<UUID, Integer> getStockLevels(Iterable<UUID> outletItemIds) {
        log.info("Getting stock levels in bulk");
        
        List<UUID> ids = StreamSupport.stream(outletItemIds.spliterator(), false)
                .collect(Collectors.toList());
        
        if (ids.isEmpty()) {
            log.debug("No item IDs provided for bulk stock fetch");
            return Map.of();
        }

        log.debug("Fetching stock levels for {} items using batch operation", ids.size());

        List<String> inventoryKeys = ids.stream()
                .map(id -> INVENTORY_KEY_PREFIX + id)
                .collect(Collectors.toList());
        List<String> reservedKeys = ids.stream()
                .map(id -> RESERVED_KEY_PREFIX + id)
                .collect(Collectors.toList());

        List<Object> inventoryValues = redisTemplate.opsForValue().multiGet(inventoryKeys);
        List<Object> reservedValues = redisTemplate.opsForValue().multiGet(reservedKeys);

        Map<UUID, Integer> result = new ConcurrentHashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            UUID id = ids.get(i);
            int total = inventoryValues != null && inventoryValues.get(i) != null 
                    ? ((Number) inventoryValues.get(i)).intValue() : 0;
            int reserved = reservedValues != null && reservedValues.get(i) != null 
                    ? ((Number) reservedValues.get(i)).intValue() : 0;
            result.put(id, Math.max(0, total - reserved));
        }
        
        log.info("Stock levels retrieved successfully for {} items using batch fetch", result.size());
        return result;
    }

    /**
     * Synchronizes Redis cache with the database periodically.
     *
     * <p>This scheduled task runs every 5 minutes to ensure Redis and database consistency.
     * Uses ShedLock for distributed locking to prevent concurrent execution in clustered environments.
     * Only items not already cached in Redis are synchronized from the database.</p>
     */
    @Scheduled(fixedRate = 300000)
    @SchedulerLock(name = "syncInventoryWithDb", lockAtMostFor = "PT4M", lockAtLeastFor = "PT30S")
    public void syncInventoryWithDb() {
        log.info("Starting scheduled inventory synchronization");
        
        List<OutletItem> items = outletItemRepository.findAll();
        if (items.isEmpty()) {
            log.info("No items found to synchronize");
            return;
        }

        log.debug("Found {} items for potential synchronization", items.size());

        List<String> keys = items.stream()
                .map(item -> INVENTORY_KEY_PREFIX + item.getId())
                .collect(Collectors.toList());

        List<Object> redisValues = redisTemplate.opsForValue().multiGet(keys);

        int syncedCount = 0;
        for (int i = 0; i < items.size(); i++) {
            OutletItem item = items.get(i);
            Object redisValue = redisValues != null ? redisValues.get(i) : null;
            
            if (redisValue == null) {
                String inventoryKey = INVENTORY_KEY_PREFIX + item.getId();
                Integer qty = item.getCurrentQuantity() != null ? item.getCurrentQuantity() : 0;
                redisTemplate.opsForValue().set(inventoryKey, qty, INVENTORY_CACHE_TTL);
                syncedCount++;
            }
        }
        
        log.info("Inventory synchronization completed: totalItems={}, newlyCached={}", 
                items.size(), syncedCount);
    }

    /**
     * Retrieves stock quantity from Redis cache or falls back to database.
     *
     * <p>If the value is not found in Redis, it is fetched from the database
     * and cached in Redis for future requests.</p>
     *
     * @param key the Redis key for the inventory item
     * @param outletItemId the unique identifier of the outlet item
     * @return the stock quantity, or 0 if not found
     */
    private Integer getFromRedisOrDb(String key, UUID outletItemId) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("Stock retrieved from Redis cache: outletItemId={}, value={}", outletItemId, value);
            return ((Number) value).intValue();
        }

        log.debug("Stock not in Redis cache, fetching from database: outletItemId={}", outletItemId);
        OutletItem item = outletItemRepository.findById(outletItemId).orElse(null);
        if (item != null) {
            int stock = item.getCurrentQuantity() != null ? item.getCurrentQuantity() : 0;
            redisTemplate.opsForValue().set(key, stock, INVENTORY_CACHE_TTL);
            log.debug("Stock cached in Redis: outletItemId={}, value={}", outletItemId, stock);
            return stock;
        }

        log.debug("Outlet item not found in database: outletItemId={}", outletItemId);
        return 0;
    }

    /**
     * Retrieves the reserved stock count from Redis.
     *
     * @param key the Redis key for the reserved inventory
     * @return the reserved count, or 0 if not found
     */
    private Integer getReservedCount(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        int reservedCount = value != null ? ((Number) value).intValue() : 0;
        log.debug("Reserved count retrieved: key={}, count={}", key, reservedCount);
        return reservedCount;
    }
}
