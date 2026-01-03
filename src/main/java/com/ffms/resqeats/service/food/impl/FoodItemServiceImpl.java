package com.ffms.resqeats.service.food.impl;

import com.ffms.resqeats.dto.food.*;
import com.ffms.resqeats.exception.food.FoodItemException;
import com.ffms.resqeats.models.food.FoodItem;
import com.ffms.resqeats.models.shop.Shop;
import com.ffms.resqeats.repository.food.FoodItemRepository;
import com.ffms.resqeats.repository.shop.ShopRepository;
import com.ffms.resqeats.service.food.FoodItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FoodItemServiceImpl implements FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final ShopRepository shopRepository;

    @Override
    public FoodItemResponse createFoodItem(CreateFoodItemRequest request, Long ownerId) {
        Shop shop = shopRepository.findByIdAndOwnerId(request.getShopId(), ownerId)
                .orElseThrow(() -> new FoodItemException("Shop not found or you don't have permission"));

        FoodItem foodItem = FoodItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .originalPrice(request.getOriginalPrice())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .allergens(request.getAllergens())
                .dietaryInfo(request.getDietaryInfo())
                .isVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false)
                .isVegan(request.getIsVegan() != null ? request.getIsVegan() : false)
                .isGlutenFree(request.getIsGlutenFree() != null ? request.getIsGlutenFree() : false)
                .expiryDate(request.getExpiryDate())
                .isActive(true)
                .shop(shop)
                .build();

        foodItem = foodItemRepository.save(foodItem);
        log.info("Food item created with id: {}", foodItem.getId());
        return mapToResponse(foodItem);
    }

    @Override
    public FoodItemResponse updateFoodItem(Long foodItemId, CreateFoodItemRequest request, Long ownerId) {
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new FoodItemException("Food item not found"));

        // Verify ownership
        if (!foodItem.getShop().getOwner().getId().equals(ownerId)) {
            throw new FoodItemException("You don't have permission to update this food item");
        }

        if (request.getName() != null) foodItem.setName(request.getName());
        if (request.getDescription() != null) foodItem.setDescription(request.getDescription());
        if (request.getOriginalPrice() != null) foodItem.setOriginalPrice(request.getOriginalPrice());
        if (request.getImageUrl() != null) foodItem.setImageUrl(request.getImageUrl());
        if (request.getCategory() != null) foodItem.setCategory(request.getCategory());
        if (request.getAllergens() != null) foodItem.setAllergens(request.getAllergens());
        if (request.getDietaryInfo() != null) foodItem.setDietaryInfo(request.getDietaryInfo());
        if (request.getIsVegetarian() != null) foodItem.setIsVegetarian(request.getIsVegetarian());
        if (request.getIsVegan() != null) foodItem.setIsVegan(request.getIsVegan());
        if (request.getIsGlutenFree() != null) foodItem.setIsGlutenFree(request.getIsGlutenFree());
        if (request.getExpiryDate() != null) foodItem.setExpiryDate(request.getExpiryDate());

        foodItem = foodItemRepository.save(foodItem);
        log.info("Food item updated with id: {}", foodItem.getId());
        return mapToResponse(foodItem);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodItemResponse getFoodItemById(Long foodItemId) {
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new FoodItemException("Food item not found"));
        return mapToResponse(foodItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponse> getFoodItemsByShop(Long shopId) {
        return foodItemRepository.findByShopId(shopId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FoodItemResponse> getFoodItemsByShop(Long shopId, Pageable pageable) {
        return foodItemRepository.findByShopId(shopId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public void deleteFoodItem(Long foodItemId, Long ownerId) {
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new FoodItemException("Food item not found"));

        if (!foodItem.getShop().getOwner().getId().equals(ownerId)) {
            throw new FoodItemException("You don't have permission to delete this food item");
        }

        foodItemRepository.delete(foodItem);
        log.info("Food item deleted with id: {}", foodItemId);
    }

    @Override
    public void deactivateFoodItem(Long foodItemId, Long ownerId) {
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new FoodItemException("Food item not found"));

        if (!foodItem.getShop().getOwner().getId().equals(ownerId)) {
            throw new FoodItemException("You don't have permission to deactivate this food item");
        }

        foodItem.setIsActive(false);
        foodItemRepository.save(foodItem);
        log.info("Food item deactivated with id: {}", foodItemId);
    }

    @Override
    public void activateFoodItem(Long foodItemId, Long ownerId) {
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new FoodItemException("Food item not found"));

        if (!foodItem.getShop().getOwner().getId().equals(ownerId)) {
            throw new FoodItemException("You don't have permission to activate this food item");
        }

        foodItem.setIsActive(true);
        foodItemRepository.save(foodItem);
        log.info("Food item activated with id: {}", foodItemId);
    }

    private FoodItemResponse mapToResponse(FoodItem foodItem) {
        return FoodItemResponse.builder()
                .id(foodItem.getId())
                .name(foodItem.getName())
                .description(foodItem.getDescription())
                .originalPrice(foodItem.getOriginalPrice())
                .imageUrl(foodItem.getImageUrl())
                .category(foodItem.getCategory())
                .allergens(foodItem.getAllergens())
                .dietaryInfo(foodItem.getDietaryInfo())
                .isVegetarian(foodItem.getIsVegetarian())
                .isVegan(foodItem.getIsVegan())
                .isGlutenFree(foodItem.getIsGlutenFree())
                .expiryDate(foodItem.getExpiryDate())
                .isActive(foodItem.getIsActive())
                .shopId(foodItem.getShop().getId())
                .shopName(foodItem.getShop().getName())
                .build();
    }
}
