package com.ffms.resqeats.exception.food;

import com.ffms.resqeats.exception.common.BaseException;
import com.ffms.resqeats.exception.common.ErrorCodes;
import lombok.Getter;

@Getter
public class FoodItemException extends BaseException {

    public FoodItemException(String message, String errorCode) {
        super(message, errorCode, ErrorCodes.CATEGORY_FOOD);
    }

    public FoodItemException(String message, String errorCode, String details) {
        super(message, errorCode, ErrorCodes.CATEGORY_FOOD, details);
    }

    public FoodItemException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, ErrorCodes.CATEGORY_FOOD, cause);
    }

    // Legacy constructor for backward compatibility
    public FoodItemException(String message) {
        super(message, ErrorCodes.SYSTEM_INTERNAL_ERROR, ErrorCodes.CATEGORY_FOOD);
    }

    // ===================== Factory Methods =====================

    public static FoodItemException notFound(Long foodItemId) {
        return new FoodItemException(
            String.format("Food item not found with id: %d", foodItemId),
            ErrorCodes.FOOD_ITEM_NOT_FOUND
        );
    }

    public static FoodItemException accessDenied(Long foodItemId) {
        return new FoodItemException(
            String.format("Access denied to food item: %d", foodItemId),
            ErrorCodes.FOOD_ITEM_ACCESS_DENIED
        );
    }

    public static FoodItemException notAvailable(Long foodItemId) {
        return new FoodItemException(
            String.format("Food item %d is not available", foodItemId),
            ErrorCodes.FOOD_ITEM_NOT_AVAILABLE
        );
    }

    public static FoodItemException expired(Long foodItemId) {
        return new FoodItemException(
            String.format("Food item %d has expired", foodItemId),
            ErrorCodes.FOOD_ITEM_EXPIRED
        );
    }

    public static FoodItemException shopNotFoundOrAccessDenied() {
        return new FoodItemException(
            "Shop not found or you don't have permission",
            ErrorCodes.SHOP_ACCESS_DENIED
        );
    }
}
