package com.ffms.resqeats.service.food.impl;

import com.ffms.resqeats.dto.food.*;
import com.ffms.resqeats.exception.food.SecretBoxException;
import com.ffms.resqeats.models.food.FoodItem;
import com.ffms.resqeats.models.food.SecretBox;
import com.ffms.resqeats.models.food.SecretBoxItem;
import com.ffms.resqeats.models.shop.Shop;
import com.ffms.resqeats.repository.food.FoodItemRepository;
import com.ffms.resqeats.repository.food.SecretBoxItemRepository;
import com.ffms.resqeats.repository.food.SecretBoxRepository;
import com.ffms.resqeats.repository.shop.ShopRepository;
import com.ffms.resqeats.service.food.SecretBoxService;
import com.ffms.resqeats.service.shop.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SecretBoxServiceImpl implements SecretBoxService {

    private final SecretBoxRepository secretBoxRepository;
    private final SecretBoxItemRepository secretBoxItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final ShopRepository shopRepository;
    private final ShopService shopService;

    @Override
    public SecretBoxResponse createSecretBox(CreateSecretBoxRequest request, Long ownerId) {
        Shop shop = shopRepository.findByIdAndOwnerId(request.getShopId(), ownerId)
                .orElseThrow(() -> new SecretBoxException("Shop not found or you don't have permission"));

        BigDecimal discountPercentage = calculateDiscountPercentage(request.getOriginalValue(), request.getDiscountedPrice());

        SecretBox secretBox = SecretBox.builder()
                .name(request.getName())
                .description(request.getDescription())
                .originalValue(request.getOriginalValue())
                .discountedPrice(request.getDiscountedPrice())
                .discountPercentage(discountPercentage)
                .imageUrl(request.getImageUrl())
                .totalQuantity(request.getTotalQuantity())
                .quantityAvailable(request.getTotalQuantity())
                .pickupStartTime(request.getPickupStartTime())
                .pickupEndTime(request.getPickupEndTime())
                .cutoffTime(request.getCutoffTime())
                .availableDate(request.getAvailableDate())
                .expiryTime(request.getExpiryTime())
                .mayContain(request.getMayContain())
                .isActive(true)
                .isVisible(true)
                .shop(shop)
                .build();

        secretBox = secretBoxRepository.save(secretBox);

        // Add food items to the box
        if (request.getFoodItemIds() != null && !request.getFoodItemIds().isEmpty()) {
            SecretBox finalSecretBox = secretBox;
            List<SecretBoxItem> boxItems = request.getFoodItemIds().stream()
                    .map(foodItemId -> {
                        FoodItem foodItem = foodItemRepository.findByIdAndShopId(foodItemId, shop.getId())
                                .orElseThrow(() -> new SecretBoxException("Food item not found or doesn't belong to this shop"));
                        return SecretBoxItem.builder()
                                .secretBox(finalSecretBox)
                                .foodItem(foodItem)
                                .quantity(1)
                                .build();
                    })
                    .collect(Collectors.toList());
            secretBoxItemRepository.saveAll(boxItems);
            secretBox.getItems().addAll(boxItems);
        }

        log.info("Secret box created with id: {}", secretBox.getId());
        return mapToResponse(secretBox, null, null);
    }

    @Override
    public SecretBoxResponse updateSecretBox(Long secretBoxId, CreateSecretBoxRequest request, Long ownerId) {
        SecretBox secretBox = secretBoxRepository.findById(secretBoxId)
                .orElseThrow(() -> new SecretBoxException("Secret box not found"));

        if (!secretBox.getShop().getOwner().getId().equals(ownerId)) {
            throw new SecretBoxException("You don't have permission to update this secret box");
        }

        if (request.getName() != null) secretBox.setName(request.getName());
        if (request.getDescription() != null) secretBox.setDescription(request.getDescription());
        if (request.getOriginalValue() != null) secretBox.setOriginalValue(request.getOriginalValue());
        if (request.getDiscountedPrice() != null) secretBox.setDiscountedPrice(request.getDiscountedPrice());
        if (request.getOriginalValue() != null && request.getDiscountedPrice() != null) {
            secretBox.setDiscountPercentage(calculateDiscountPercentage(request.getOriginalValue(), request.getDiscountedPrice()));
        }
        if (request.getImageUrl() != null) secretBox.setImageUrl(request.getImageUrl());
        if (request.getTotalQuantity() != null) {
            int diff = request.getTotalQuantity() - secretBox.getTotalQuantity();
            secretBox.setTotalQuantity(request.getTotalQuantity());
            secretBox.setQuantityAvailable(Math.max(0, secretBox.getQuantityAvailable() + diff));
        }
        if (request.getPickupStartTime() != null) secretBox.setPickupStartTime(request.getPickupStartTime());
        if (request.getPickupEndTime() != null) secretBox.setPickupEndTime(request.getPickupEndTime());
        if (request.getCutoffTime() != null) secretBox.setCutoffTime(request.getCutoffTime());
        if (request.getAvailableDate() != null) secretBox.setAvailableDate(request.getAvailableDate());
        if (request.getExpiryTime() != null) secretBox.setExpiryTime(request.getExpiryTime());
        if (request.getMayContain() != null) secretBox.setMayContain(request.getMayContain());

        // Update food items if provided
        if (request.getFoodItemIds() != null) {
            secretBoxItemRepository.deleteBySecretBoxId(secretBoxId);
            secretBox.getItems().clear();

            SecretBox finalSecretBox = secretBox;
            List<SecretBoxItem> boxItems = request.getFoodItemIds().stream()
                    .map(foodItemId -> {
                        FoodItem foodItem = foodItemRepository.findByIdAndShopId(foodItemId, finalSecretBox.getShop().getId())
                                .orElseThrow(() -> new SecretBoxException("Food item not found or doesn't belong to this shop"));
                        return SecretBoxItem.builder()
                                .secretBox(finalSecretBox)
                                .foodItem(foodItem)
                                .quantity(1)
                                .build();
                    })
                    .collect(Collectors.toList());
            secretBoxItemRepository.saveAll(boxItems);
            secretBox.getItems().addAll(boxItems);
        }

        secretBox = secretBoxRepository.save(secretBox);
        log.info("Secret box updated with id: {}", secretBox.getId());
        return mapToResponse(secretBox, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public SecretBoxResponse getSecretBoxById(Long secretBoxId) {
        SecretBox secretBox = secretBoxRepository.findById(secretBoxId)
                .orElseThrow(() -> new SecretBoxException("Secret box not found"));
        return mapToResponse(secretBox, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecretBoxResponse> getSecretBoxesByShop(Long shopId) {
        return secretBoxRepository.findByShopId(shopId).stream()
                .map(box -> mapToResponse(box, null, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SecretBoxResponse> getSecretBoxesByShop(Long shopId, Pageable pageable) {
        return secretBoxRepository.findByShopId(shopId, pageable)
                .map(box -> mapToResponse(box, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecretBoxResponse> getAvailableBoxesByShop(Long shopId) {
        return secretBoxRepository.findAvailableBoxesByShopId(shopId).stream()
                .map(box -> mapToResponse(box, null, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SecretBoxResponse> getAllAvailableBoxes(Pageable pageable) {
        return secretBoxRepository.findAllAvailableBoxes(pageable)
                .map(box -> mapToResponse(box, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecretBoxResponse> getNearbyAvailableBoxes(BigDecimal latitude, BigDecimal longitude, Double radiusKm) {
        return secretBoxRepository.findNearbyAvailableBoxes(latitude.doubleValue(), longitude.doubleValue(), radiusKm).stream()
                .map(box -> mapToResponse(box, latitude, longitude))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSecretBox(Long secretBoxId, Long ownerId) {
        SecretBox secretBox = secretBoxRepository.findById(secretBoxId)
                .orElseThrow(() -> new SecretBoxException("Secret box not found"));

        if (!secretBox.getShop().getOwner().getId().equals(ownerId)) {
            throw new SecretBoxException("You don't have permission to delete this secret box");
        }

        secretBoxRepository.delete(secretBox);
        log.info("Secret box deleted with id: {}", secretBoxId);
    }

    @Override
    public void deactivateSecretBox(Long secretBoxId, Long ownerId) {
        SecretBox secretBox = secretBoxRepository.findById(secretBoxId)
                .orElseThrow(() -> new SecretBoxException("Secret box not found"));

        if (!secretBox.getShop().getOwner().getId().equals(ownerId)) {
            throw new SecretBoxException("You don't have permission to deactivate this secret box");
        }

        secretBox.setIsActive(false);
        secretBoxRepository.save(secretBox);
        log.info("Secret box deactivated with id: {}", secretBoxId);
    }

    @Override
    public void activateSecretBox(Long secretBoxId, Long ownerId) {
        SecretBox secretBox = secretBoxRepository.findById(secretBoxId)
                .orElseThrow(() -> new SecretBoxException("Secret box not found"));

        if (!secretBox.getShop().getOwner().getId().equals(ownerId)) {
            throw new SecretBoxException("You don't have permission to activate this secret box");
        }

        secretBox.setIsActive(true);
        secretBoxRepository.save(secretBox);
        log.info("Secret box activated with id: {}", secretBoxId);
    }

    @Override
    public boolean updateQuantity(Long secretBoxId, int quantityChange) {
        SecretBox secretBox = secretBoxRepository.findByIdWithLock(secretBoxId)
                .orElseThrow(() -> new SecretBoxException("Secret box not found"));

        int newQuantity = secretBox.getQuantityAvailable() + quantityChange;
        if (newQuantity < 0) {
            return false;
        }

        secretBox.setQuantityAvailable(Math.min(newQuantity, secretBox.getTotalQuantity()));
        secretBoxRepository.save(secretBox);
        log.info("Secret box {} quantity updated to {}", secretBoxId, secretBox.getQuantityAvailable());
        return true;
    }

    @Override
    public SecretBoxResponse updateBoxQuantity(Long secretBoxId, Integer newQuantity, Long ownerId) {
        SecretBox secretBox = secretBoxRepository.findById(secretBoxId)
                .orElseThrow(() -> new SecretBoxException("Secret box not found"));

        if (!secretBox.getShop().getOwner().getId().equals(ownerId)) {
            throw new SecretBoxException("You don't have permission to update this secret box");
        }

        if (newQuantity < 0) {
            throw new SecretBoxException("Quantity cannot be negative");
        }

        secretBox.setQuantityAvailable(newQuantity);
        if (newQuantity > secretBox.getTotalQuantity()) {
            secretBox.setTotalQuantity(newQuantity);
        }

        secretBox = secretBoxRepository.save(secretBox);
        log.info("Secret box {} quantity manually updated to {}", secretBoxId, newQuantity);
        return mapToResponse(secretBox, null, null);
    }

    private BigDecimal calculateDiscountPercentage(BigDecimal originalValue, BigDecimal discountedPrice) {
        if (originalValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = originalValue.subtract(discountedPrice);
        return discount.multiply(BigDecimal.valueOf(100))
                .divide(originalValue, 2, RoundingMode.HALF_UP);
    }

    private SecretBoxResponse mapToResponse(SecretBox secretBox, BigDecimal userLat, BigDecimal userLon) {
        Double distance = null;
        if (userLat != null && userLon != null) {
            distance = shopService.calculateDistance(userLat, userLon,
                    secretBox.getShop().getLatitude(), secretBox.getShop().getLongitude());
            distance = Math.round(distance * 100.0) / 100.0;
        }

        List<SecretBoxItemResponse> itemResponses = new ArrayList<>();
        if (secretBox.getItems() != null) {
            itemResponses = secretBox.getItems().stream()
                    .map(item -> SecretBoxItemResponse.builder()
                            .id(item.getId())
                            .foodItemId(item.getFoodItem().getId())
                            .foodItemName(item.getFoodItem().getName())
                            .quantity(item.getQuantity())
                            .notes(item.getNotes())
                            .build())
                    .collect(Collectors.toList());
        }

        return SecretBoxResponse.builder()
                .id(secretBox.getId())
                .name(secretBox.getName())
                .description(secretBox.getDescription())
                .originalValue(secretBox.getOriginalValue())
                .discountedPrice(secretBox.getDiscountedPrice())
                .discountPercentage(secretBox.getDiscountPercentage())
                .imageUrl(secretBox.getImageUrl())
                .quantityAvailable(secretBox.getQuantityAvailable())
                .totalQuantity(secretBox.getTotalQuantity())
                .pickupStartTime(secretBox.getPickupStartTime())
                .pickupEndTime(secretBox.getPickupEndTime())
                .cutoffTime(secretBox.getCutoffTime())
                .availableDate(secretBox.getAvailableDate())
                .expiryTime(secretBox.getExpiryTime())
                .isActive(secretBox.getIsActive())
                .isVisible(secretBox.getIsVisible())
                .mayContain(secretBox.getMayContain())
                .shopId(secretBox.getShop().getId())
                .shopName(secretBox.getShop().getName())
                .shopAddress(secretBox.getShop().getAddress())
                .shopImageUrl(secretBox.getShop().getImageUrl())
                .distanceKm(distance)
                .items(itemResponses)
                .build();
    }
}
