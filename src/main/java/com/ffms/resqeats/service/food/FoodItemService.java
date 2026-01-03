package com.ffms.resqeats.service.food;

import com.ffms.resqeats.dto.food.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FoodItemService {

    FoodItemResponse createFoodItem(CreateFoodItemRequest request, Long ownerId);

    FoodItemResponse updateFoodItem(Long foodItemId, CreateFoodItemRequest request, Long ownerId);

    FoodItemResponse getFoodItemById(Long foodItemId);

    List<FoodItemResponse> getFoodItemsByShop(Long shopId);

    Page<FoodItemResponse> getFoodItemsByShop(Long shopId, Pageable pageable);

    void deleteFoodItem(Long foodItemId, Long ownerId);

    void deactivateFoodItem(Long foodItemId, Long ownerId);

    void activateFoodItem(Long foodItemId, Long ownerId);
}
