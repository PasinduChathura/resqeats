package com.ffms.resqeats.item.service;

import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.inventory.service.InventoryService;
import com.ffms.resqeats.item.dto.CreateItemRequest;
import com.ffms.resqeats.item.dto.ItemDto;
import com.ffms.resqeats.item.dto.ItemFilterDto;
import com.ffms.resqeats.item.dto.ItemListResponseDto;
import com.ffms.resqeats.item.dto.OutletItemDto;
import com.ffms.resqeats.item.dto.UpdateItemRequest;
import com.ffms.resqeats.item.entity.Item;
import com.ffms.resqeats.item.entity.OutletItem;
import com.ffms.resqeats.item.enums.ItemStatus;
import com.ffms.resqeats.item.enums.ItemType;
import com.ffms.resqeats.item.repository.ItemRepository;
import com.ffms.resqeats.item.repository.OutletItemRepository;
import com.ffms.resqeats.item.specification.ItemSpecification;
import com.ffms.resqeats.merchant.entity.Merchant;
import com.ffms.resqeats.merchant.repository.MerchantRepository;
import com.ffms.resqeats.outlet.entity.Outlet;
import com.ffms.resqeats.outlet.repository.OutletRepository;
import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.repository.UserRepository;
import com.ffms.resqeats.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing food items and outlet item inventory.
 *
 * <p>This service handles the complete lifecycle of items within the ResqEats platform,
 * including individual food items (ITEM type) and secret/surprise boxes (SECRET_BOX type).
 * Items are created at the merchant level and can be assigned to specific outlets with
 * individual quantity tracking.</p>
 *
 * <p>Business Rules enforced by this service:</p>
 * <ul>
 *   <li>BR-022: Items must have sale price less than or equal to base price</li>
 *   <li>BR-023: Secret boxes must have minimum 30% discount</li>
 *   <li>BR-024: Items automatically marked sold out when quantity reaches zero</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 * @see Item
 * @see OutletItem
 * @see InventoryService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final OutletItemRepository outletItemRepository;
    private final OutletRepository outletRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final WebSocketService webSocketService;

    /**
     * Minimum discount percentage required for secret boxes (30%).
     */
    private static final BigDecimal MIN_SECRET_BOX_DISCOUNT = new BigDecimal("0.30");

    /**
     * Creates a new item for a merchant.
     *
     * <p>Creates an item at the merchant level with the specified details. The item can later
     * be assigned to specific outlets with individual quantity tracking. Request uses
     * originalPrice/discountedPrice terminology while entity uses basePrice/salePrice.</p>
     *
     * @param merchantId the unique identifier of the merchant
     * @param request the item creation request containing item details
     * @param userId the unique identifier of the user performing the action
     * @return the created item as a DTO
     * @throws BusinessException when merchant access validation fails or pricing rules are violated
     */
    @Transactional
    public ItemDto createItem(Long merchantId, CreateItemRequest request, Long userId) {
        log.info("Creating item for merchant: {}, name: {}, type: {}, userId: {}", 
                merchantId, request.getName(), request.getType(), userId);
        
        validateMerchantAccess(merchantId, userId);
        log.debug("Merchant access validated for merchantId: {}", merchantId);
        
        validatePricing(request.getOriginalPrice(), request.getDiscountedPrice(), request.getType());
        log.debug("Pricing validation passed - originalPrice: {}, discountedPrice: {}", 
                request.getOriginalPrice(), request.getDiscountedPrice());

        Map<String, Object> dietaryInfo = null;
        if (request.getDietaryInfo() != null && !request.getDietaryInfo().isEmpty()) {
            dietaryInfo = Map.of("tags", request.getDietaryInfo());
        }
        if (request.getAllergens() != null && !request.getAllergens().isEmpty()) {
            if (dietaryInfo == null) {
                dietaryInfo = Map.of("allergens", request.getAllergens());
            } else {
                dietaryInfo = Map.of("tags", request.getDietaryInfo(), "allergens", request.getAllergens());
            }
        }

        Item item = Item.builder()
                .merchantId(merchantId)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .itemType(request.getType())
                .basePrice(request.getOriginalPrice())
                .salePrice(request.getDiscountedPrice())
                .imageUrl(request.getImageUrl())
                .dietaryInfo(dietaryInfo)
                .status(ItemStatus.ACTIVE)
                .build();

        item = itemRepository.save(item);
        log.info("Item created successfully - itemId: {}, merchantId: {}, name: {}", 
                item.getId(), merchantId, item.getName());
        return toItemDto(item);
    }

    /**
     * Updates an existing item with new details.
     *
     * <p>Allows partial updates to item properties including name, description, category,
     * image URL, dietary information, and pricing. Request uses originalPrice/discountedPrice
     * terminology while entity uses basePrice/salePrice.</p>
     *
     * @param itemId the unique identifier of the item to update
     * @param request the update request containing fields to modify
     * @param userId the unique identifier of the user performing the action
     * @return the updated item as a DTO
     * @throws BusinessException when item is not found, access validation fails, or pricing rules are violated
     */
    @Transactional
    public ItemDto updateItem(Long itemId, UpdateItemRequest request, Long userId) {
        log.info("Updating item: {}, userId: {}", itemId, userId);
        
        Item item = getItemOrThrow(itemId);
        validateMerchantAccess(item.getMerchantId(), userId);
        log.debug("Merchant access validated for item update - itemId: {}, merchantId: {}", 
                itemId, item.getMerchantId());

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        
        if (request.getAllergens() != null || request.getDietaryInfo() != null) {
            Map<String, Object> dietaryInfo = item.getDietaryInfo();
            if (dietaryInfo == null) {
                dietaryInfo = new java.util.HashMap<>();
            }
            if (request.getAllergens() != null) {
                dietaryInfo.put("allergens", request.getAllergens());
            }
            if (request.getDietaryInfo() != null) {
                dietaryInfo.put("tags", request.getDietaryInfo());
            }
            item.setDietaryInfo(dietaryInfo);
        }

        if (request.getOriginalPrice() != null || request.getDiscountedPrice() != null) {
            BigDecimal basePrice = request.getOriginalPrice() != null 
                    ? request.getOriginalPrice() : item.getBasePrice();
            BigDecimal salePrice = request.getDiscountedPrice() != null 
                    ? request.getDiscountedPrice() : item.getSalePrice();
            validatePricing(basePrice, salePrice, item.getItemType());
            log.debug("Pricing validation passed for update - basePrice: {}, salePrice: {}", 
                    basePrice, salePrice);
            item.setBasePrice(basePrice);
            item.setSalePrice(salePrice);
        }

        item = itemRepository.save(item);
        log.info("Item updated successfully - itemId: {}, name: {}", itemId, item.getName());
        return toItemDto(item);
    }

    /**
     * Adds an item to an outlet with specified quantity.
     *
     * <p>Associates a merchant-level item with a specific outlet and sets the initial
     * quantity available for sale. Also initializes the inventory tracking in Redis
     * for real-time stock management.</p>
     *
     * @param outletId the unique identifier of the outlet
     * @param itemId the unique identifier of the item to add
     * @param quantity the initial quantity available at this outlet
     * @param userId the unique identifier of the user performing the action
     * @return the created outlet item association as a DTO
     * @throws BusinessException when outlet or item is not found, item doesn't belong to merchant,
     *                           item already exists at outlet, or access validation fails
     */
    @Transactional
    public OutletItemDto addItemToOutlet(Long outletId, Long itemId, int quantity, Long userId) {
        log.info("Adding item to outlet - outletId: {}, itemId: {}, quantity: {}, userId: {}", 
                outletId, itemId, quantity, userId);
        
        Outlet outlet = outletRepository.findById(outletId)
                .orElseThrow(() -> {
                    log.warn("Outlet not found - outletId: {}", outletId);
                    return new BusinessException("OUTLET_004", "Outlet not found");
                });
        validateOutletAccess(outlet, userId);

        Item item = getItemOrThrow(itemId);
        if (!item.getMerchantId().equals(outlet.getMerchantId())) {
            log.warn("Item does not belong to merchant - itemId: {}, itemMerchantId: {}, outletMerchantId: {}", 
                    itemId, item.getMerchantId(), outlet.getMerchantId());
            throw new BusinessException("ITEM_001", "Item does not belong to this merchant");
        }

        if (outletItemRepository.existsByOutletIdAndItemId(outletId, itemId)) {
            log.warn("Item already exists at outlet - outletId: {}, itemId: {}", outletId, itemId);
            throw new BusinessException("ITEM_002", "Item already added to this outlet");
        }

        OutletItem outletItem = OutletItem.builder()
                .outletId(outletId)
                .itemId(itemId)
                .dailyQuantity(quantity)
                .currentQuantity(quantity)
                .isAvailable(true)
                .build();

        outletItem = outletItemRepository.save(outletItem);

        inventoryService.initializeStock(outletItem.getId());
        log.debug("Inventory initialized in Redis for outletItemId: {}", outletItem.getId());

        log.info("Item added to outlet successfully - outletItemId: {}, outletId: {}, itemId: {}", 
                outletItem.getId(), outletId, itemId);
        return toOutletItemDto(outletItem, item);
    }

    /**
     * Updates the quantity of an item at a specific outlet.
     *
     * <p>Updates both the daily and current quantity for an outlet item and synchronizes
     * the stock with the Redis inventory service for real-time tracking.</p>
     *
     * @param outletItemId the unique identifier of the outlet item association
     * @param quantity the new quantity to set
     * @param userId the unique identifier of the user performing the action
     * @return the updated outlet item as a DTO
     * @throws BusinessException when outlet item or outlet is not found, or access validation fails
     */
    @Transactional
    public OutletItemDto updateOutletItem(Long outletItemId, int quantity, Long userId) {
        log.info("Updating outlet item quantity - outletItemId: {}, newQuantity: {}, userId: {}", 
                outletItemId, quantity, userId);
        
        OutletItem outletItem = outletItemRepository.findById(outletItemId)
                .orElseThrow(() -> {
                    log.warn("Outlet item not found - outletItemId: {}", outletItemId);
                    return new BusinessException("ITEM_003", "Outlet item not found");
                });

        Outlet outlet = outletRepository.findById(outletItem.getOutletId())
                .orElseThrow(() -> {
                    log.error("Outlet not found for outlet item - outletItemId: {}, outletId: {}", 
                            outletItemId, outletItem.getOutletId());
                    return new BusinessException("OUTLET_004", "Outlet not found");
                });
        validateOutletAccess(outlet, userId);

        Item item = itemRepository.findById(outletItem.getItemId()).orElse(null);
        log.debug("Previous quantity - daily: {}, current: {}", 
                outletItem.getDailyQuantity(), outletItem.getCurrentQuantity());

        outletItem.setCurrentQuantity(quantity);
        outletItem.setDailyQuantity(quantity);

        OutletItem savedOutletItem = outletItemRepository.save(outletItem);

        inventoryService.setStock(outletItemId, quantity);
        log.debug("Inventory updated in Redis for outletItemId: {}", outletItemId);

        log.info("Outlet item updated successfully - outletItemId: {}, newQuantity: {}", 
                outletItemId, quantity);
        return toOutletItemDto(savedOutletItem, item);
    }

    /**
     * Removes an item from an outlet by marking it as unavailable.
     *
     * <p>Performs a soft delete by setting the item's availability flag to false
     * rather than physically removing the record.</p>
     *
     * @param outletItemId the unique identifier of the outlet item to remove
     * @param userId the unique identifier of the user performing the action
     * @throws BusinessException when outlet item or outlet is not found, or access validation fails
     */
    @Transactional
    public void removeItemFromOutlet(Long outletItemId, Long userId) {
        log.info("Removing item from outlet - outletItemId: {}, userId: {}", outletItemId, userId);
        
        OutletItem outletItem = outletItemRepository.findById(outletItemId)
                .orElseThrow(() -> {
                    log.warn("Outlet item not found for removal - outletItemId: {}", outletItemId);
                    return new BusinessException("ITEM_003", "Outlet item not found");
                });

        Outlet outlet = outletRepository.findById(outletItem.getOutletId())
                .orElseThrow(() -> {
                    log.error("Outlet not found for outlet item removal - outletItemId: {}, outletId: {}", 
                            outletItemId, outletItem.getOutletId());
                    return new BusinessException("OUTLET_004", "Outlet not found");
                });
        validateOutletAccess(outlet, userId);

        outletItem.setIsAvailable(false);
        outletItemRepository.save(outletItem);

        log.info("Item removed from outlet successfully - outletItemId: {}, outletId: {}", 
                outletItemId, outlet.getId());
    }

    /**
     * Marks an outlet item as sold out by setting its quantity to zero.
     *
     * <p>This method is automatically triggered per BR-024 when item quantity reaches zero.
     * It broadcasts a sold-out notification via WebSocket to notify connected clients.</p>
     *
     * @param outletItemId the unique identifier of the outlet item to mark as sold out
     */
    @Transactional
    public void markSoldOut(Long outletItemId) {
        log.info("Marking item as sold out - outletItemId: {}", outletItemId);
        
        OutletItem outletItem = outletItemRepository.findById(outletItemId).orElse(null);
        if (outletItem != null) {
            outletItem.setCurrentQuantity(0);
            outletItemRepository.save(outletItem);
            log.debug("Outlet item quantity set to zero - outletItemId: {}", outletItemId);

            Item item = itemRepository.findById(outletItem.getItemId()).orElse(null);
            if (item != null) {
                webSocketService.broadcastItemSoldOut(outletItem.getOutletId(), item.getId(), item.getName());
                log.info("Item marked as sold out and notification broadcast - outletItemId: {}, itemName: {}", 
                        outletItemId, item.getName());
            } else {
                log.warn("Item not found for sold out notification - itemId: {}", outletItem.getItemId());
            }
        } else {
            log.warn("Outlet item not found for sold out marking - outletItemId: {}", outletItemId);
        }
    }

    /**
     * Retrieves all available items for a specific outlet (customer view).
     *
     * <p>Returns items that are marked as available and have ACTIVE status.
     * This is the primary method for customers browsing items at an outlet.</p>
     *
     * @param outletId the unique identifier of the outlet
     * @return list of available outlet items as DTOs
     */
    public List<OutletItemDto> getOutletItems(Long outletId) {
        log.info("Fetching outlet items for customer view - outletId: {}", outletId);
        
        List<OutletItemDto> items = outletItemRepository.findByOutletIdAndIsAvailableTrue(outletId).stream()
                .map(oi -> {
                    Item item = itemRepository.findById(oi.getItemId()).orElse(null);
                    return toOutletItemDto(oi, item);
                })
                .filter(dto -> dto.getItem() != null && dto.getItem().getStatus() == ItemStatus.ACTIVE)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} available items for outlet: {}", items.size(), outletId);
        return items;
    }

    /**
     * Retrieves available items for an outlet with real-time stock information.
     *
     * <p>Returns items that have stock greater than zero, including real-time
     * quantity information from the Redis inventory service.</p>
     *
     * @param outletId the unique identifier of the outlet
     * @return list of available outlet items with real-time stock as DTOs
     */
    public List<OutletItemDto> getAvailableOutletItems(Long outletId) {
        log.info("Fetching available outlet items with real-time stock - outletId: {}", outletId);
        
        List<OutletItemDto> items = outletItemRepository.findAvailableByOutletId(outletId).stream()
                .map(oi -> {
                    Item item = itemRepository.findById(oi.getItemId()).orElse(null);
                    OutletItemDto dto = toOutletItemDto(oi, item);
                    dto.setRealTimeQuantity(inventoryService.getAvailableStock(oi.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        
        log.info("Retrieved {} items with stock > 0 for outlet: {}", items.size(), outletId);
        return items;
    }

    /**
     * Retrieves paginated items for a specific merchant.
     *
     * <p>Returns all active items belonging to the merchant with pagination support.</p>
     *
     * @param merchantId the unique identifier of the merchant
     * @param pageable pagination parameters
     * @return paginated list of merchant items as DTOs
     */
    public Page<ItemDto> getMerchantItems(Long merchantId, Pageable pageable) {
        log.info("Fetching merchant items - merchantId: {}, page: {}, size: {}", 
                merchantId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ItemDto> items = itemRepository.findByMerchantIdAndStatus(merchantId, ItemStatus.ACTIVE, pageable)
                .map(this::toItemDto);
        
        log.info("Retrieved {} items for merchant: {} (total: {})", 
                items.getNumberOfElements(), merchantId, items.getTotalElements());
        return items;
    }

    /**
     * Retrieves all items with comprehensive filtering.
     *
     * @param filter the filter criteria
     * @param pageable the pagination parameters
     * @return a page of filtered items as list response DTOs
     */
    public Page<ItemListResponseDto> getAllItems(ItemFilterDto filter, Pageable pageable) {
        log.info("Retrieving all items with filter: {}, page: {}, size: {}", 
                filter, pageable.getPageNumber(), pageable.getPageSize());
        Page<ItemListResponseDto> items = itemRepository.findAll(ItemSpecification.filterBy(filter), pageable)
                .map(this::toListDto);
        log.info("Retrieved {} items", items.getTotalElements());
        return items;
    }

    /**
     * Searches for items by name with pagination support.
     *
     * <p>Performs a case-insensitive search on item names and returns matching
     * items along with their first available outlet association.</p>
     *
     * @param query the search query string
     * @param pageable pagination parameters
     * @return paginated list of matching outlet items as DTOs
     */
    public Page<OutletItemDto> searchItems(String query, Pageable pageable) {
        log.info("Searching items - query: '{}', page: {}, size: {}", 
                query, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<OutletItemDto> results = itemRepository.findByNameContainingIgnoreCase(query, pageable)
                .map(item -> {
                    OutletItem outletItem = outletItemRepository.findFirstByItemIdAndIsAvailableTrue(item.getId())
                            .orElse(null);
                    return toOutletItemDto(outletItem, item);
                });
        
        log.info("Search completed - query: '{}', found: {} items (total: {})", 
                query, results.getNumberOfElements(), results.getTotalElements());
        return results;
    }

    /**
     * Retrieves a single item by its unique identifier.
     *
     * @param itemId the unique identifier of the item
     * @return the item as a DTO
     * @throws BusinessException when item is not found
     */
    public ItemDto getItem(Long itemId) {
        log.info("Fetching item - itemId: {}", itemId);
        ItemDto item = toItemDto(getItemOrThrow(itemId));
        log.info("Item retrieved successfully - itemId: {}, name: {}", itemId, item.getName());
        return item;
    }

    /**
     * Validates item pricing according to business rules.
     *
     * <p>Enforces the following pricing rules:</p>
     * <ul>
     *   <li>BR-022: Sale price must be less than or equal to base price</li>
     *   <li>BR-023: Secret boxes must have at least 30% discount</li>
     * </ul>
     *
     * @param basePrice the original/base price of the item
     * @param salePrice the discounted/sale price of the item
     * @param type the type of item (ITEM or SECRET_BOX)
     * @throws BusinessException when pricing rules are violated
     */
    private void validatePricing(BigDecimal basePrice, BigDecimal salePrice, ItemType type) {
        log.debug("Validating pricing - basePrice: {}, salePrice: {}, type: {}", basePrice, salePrice, type);
        
        if (salePrice.compareTo(basePrice) > 0) {
            log.warn("Pricing validation failed - discounted price exceeds original price: {} > {}", 
                    salePrice, basePrice);
            throw new BusinessException("ITEM_004", "Discounted price cannot exceed original price");
        }

        if (type == ItemType.SECRET_BOX) {
            BigDecimal discountPercent = basePrice.subtract(salePrice)
                    .divide(basePrice, 2, RoundingMode.HALF_UP);
            if (discountPercent.compareTo(MIN_SECRET_BOX_DISCOUNT) < 0) {
                log.warn("Secret box discount validation failed - discount: {}%, required: {}%", 
                        discountPercent.multiply(BigDecimal.valueOf(100)), 
                        MIN_SECRET_BOX_DISCOUNT.multiply(BigDecimal.valueOf(100)));
                throw new BusinessException("ITEM_005", "Secret boxes must have at least 30% discount");
            }
            log.debug("Secret box discount validated - discount: {}%", 
                    discountPercent.multiply(BigDecimal.valueOf(100)));
        }
    }

    /**
     * Retrieves an item by ID or throws an exception if not found.
     *
     * @param itemId the unique identifier of the item
     * @return the item entity
     * @throws BusinessException when item is not found
     */
    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Item not found - itemId: {}", itemId);
                    return new BusinessException("ITEM_006", "Item not found");
                });
    }

    /**
     * Validates that the user has access to manage items for the specified merchant.
     *
     * <p>Admin users have access to all merchants. Non-admin users must be the owner
     * of the merchant to have access.</p>
     *
     * @param merchantId the unique identifier of the merchant
     * @param userId the unique identifier of the user
     * @throws BusinessException when user is not found or does not have access
     */
    private void validateMerchantAccess(Long merchantId, Long userId) {
        log.debug("Validating merchant access - merchantId: {}, userId: {}", merchantId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found during merchant access validation - userId: {}", userId);
                    return new BusinessException("AUTH_003", "User not found");
                });

        if (user.getRole() == UserRole.ADMIN) {
            log.debug("Admin access granted for merchant: {}", merchantId);
            return;
        }

        Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
        if (merchant == null || !merchant.getOwnerUserId().equals(userId)) {
            log.warn("Merchant access denied - merchantId: {}, userId: {}, merchantOwnerId: {}", 
                    merchantId, userId, merchant != null ? merchant.getOwnerUserId() : "null");
            throw new BusinessException("AUTH_003", "Not authorized to manage items for this merchant");
        }
        log.debug("Merchant owner access granted - merchantId: {}, userId: {}", merchantId, userId);
    }

    /**
     * Validates that the user has access to manage the specified outlet.
     *
     * <p>Access is granted to:</p>
     * <ul>
     *   <li>Admin users (access to all outlets)</li>
     *   <li>Merchant owners (access to their merchant's outlets)</li>
     *   <li>Outlet users (access to their assigned outlet)</li>
     * </ul>
     *
     * @param outlet the outlet entity to validate access for
     * @param userId the unique identifier of the user
     * @throws BusinessException when user is not found or does not have access
     */
    private void validateOutletAccess(Outlet outlet, Long userId) {
        log.debug("Validating outlet access - outletId: {}, userId: {}", outlet.getId(), userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found during outlet access validation - userId: {}", userId);
                    return new BusinessException("AUTH_003", "User not found");
                });

        if (user.getRole() == UserRole.ADMIN) {
            log.debug("Admin access granted for outlet: {}", outlet.getId());
            return;
        }

        Merchant merchant = merchantRepository.findById(outlet.getMerchantId()).orElse(null);
        if (merchant != null && merchant.getOwnerUserId().equals(userId)) {
            log.debug("Merchant owner access granted for outlet: {}", outlet.getId());
            return;
        }

        if (user.getRole() == UserRole.OUTLET_USER && outlet.getId().equals(user.getOutletId())) {
            log.debug("Outlet user access granted for outlet: {}", outlet.getId());
            return;
        }

        log.warn("Outlet access denied - outletId: {}, userId: {}, userRole: {}", 
                outlet.getId(), userId, user.getRole());
        throw new BusinessException("AUTH_003", "Not authorized to manage this outlet");
    }

    /**
     * Converts an Item entity to an ItemDto.
     *
     * <p>Extracts allergens and dietary tags from the dietary info map and calculates
     * the discount percentage based on base and sale prices.</p>
     *
     * @param item the item entity to convert
     * @return the item as a DTO
     */
    @SuppressWarnings("unchecked")
    private ItemDto toItemDto(Item item) {
        log.debug("Converting item to DTO - itemId: {}", item.getId());
        
        List<String> allergens = null;
        List<String> dietaryTags = null;
        if (item.getDietaryInfo() != null) {
            Object allergensObj = item.getDietaryInfo().get("allergens");
            if (allergensObj instanceof List) {
                allergens = (List<String>) allergensObj;
            }
            Object tagsObj = item.getDietaryInfo().get("tags");
            if (tagsObj instanceof List) {
                dietaryTags = (List<String>) tagsObj;
            }
        }

        return ItemDto.builder()
                .id(item.getId())
                .merchantId(item.getMerchantId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .type(item.getItemType())
                .originalPrice(item.getBasePrice())
                .discountedPrice(item.getSalePrice())
                .imageUrl(item.getImageUrl())
                .allergens(allergens)
                .dietaryInfo(dietaryTags)
                .status(item.getStatus())
                .discountPercent(calculateDiscountPercent(item.getBasePrice(), item.getSalePrice()))
                .createdAt(item.getCreatedAt())
                .build();
    }

    /**
     * Converts an OutletItem entity along with its associated Item to an OutletItemDto.
     *
     * @param outletItem the outlet item entity to convert (may be null)
     * @param item the associated item entity (may be null)
     * @return the outlet item as a DTO
     */
    private OutletItemDto toOutletItemDto(OutletItem outletItem, Item item) {
        OutletItemDto dto = new OutletItemDto();
        
        if (outletItem != null) {
            dto.setOutletItemId(outletItem.getId());
            dto.setOutletId(outletItem.getOutletId());
            dto.setQuantityAvailable(outletItem.getCurrentQuantity());
            if (item != null) {
                dto.setDiscountedPrice(item.getSalePrice());
            }
            dto.setIsActive(outletItem.getIsAvailable());
        }

        if (item != null) {
            dto.setItem(toItemDto(item));
        }

        return dto;
    }

    /**
     * Calculates the discount percentage between base and sale prices.
     *
     * @param basePrice the original/base price
     * @param salePrice the discounted/sale price
     * @return the discount percentage as an integer (0-100)
     */
    private int calculateDiscountPercent(BigDecimal basePrice, BigDecimal salePrice) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return basePrice.subtract(salePrice)
                .divide(basePrice, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }

    /**
     * Converts an Item entity to an ItemListResponseDto for list display.
     *
     * @param item the item entity to convert
     * @return the item as a list response DTO
     */
    private ItemListResponseDto toListDto(Item item) {
        log.debug("Converting item to list DTO - itemId: {}", item.getId());
        
        return ItemListResponseDto.builder()
                .id(item.getId())
                .merchantId(item.getMerchantId())
                .name(item.getName())
                .category(item.getCategory())
                .itemType(item.getItemType())
                .basePrice(item.getBasePrice())
                .salePrice(item.getSalePrice())
                .discountPercentage(BigDecimal.valueOf(calculateDiscountPercent(item.getBasePrice(), item.getSalePrice())))
                .imageUrl(item.getImageUrl())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
