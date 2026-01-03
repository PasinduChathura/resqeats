package com.ffms.resqeats.exception.food;

import com.ffms.resqeats.exception.common.BaseException;
import com.ffms.resqeats.exception.common.ErrorCodes;
import lombok.Getter;

@Getter
public class SecretBoxException extends BaseException {

    public SecretBoxException(String message, String errorCode) {
        super(message, errorCode, ErrorCodes.CATEGORY_FOOD);
    }

    public SecretBoxException(String message, String errorCode, String details) {
        super(message, errorCode, ErrorCodes.CATEGORY_FOOD, details);
    }

    public SecretBoxException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, ErrorCodes.CATEGORY_FOOD, cause);
    }

    // Legacy constructor for backward compatibility
    public SecretBoxException(String message) {
        super(message, ErrorCodes.SYSTEM_INTERNAL_ERROR, ErrorCodes.CATEGORY_FOOD);
    }

    // ===================== Factory Methods =====================

    public static SecretBoxException notFound(Long secretBoxId) {
        return new SecretBoxException(
            String.format("Secret box not found with id: %d", secretBoxId),
            ErrorCodes.SECRET_BOX_NOT_FOUND
        );
    }

    public static SecretBoxException accessDenied(Long secretBoxId) {
        return new SecretBoxException(
            String.format("Access denied to secret box: %d", secretBoxId),
            ErrorCodes.SECRET_BOX_ACCESS_DENIED
        );
    }

    public static SecretBoxException notAvailable(Long secretBoxId) {
        return new SecretBoxException(
            String.format("Secret box %d is not available", secretBoxId),
            ErrorCodes.SECRET_BOX_NOT_AVAILABLE
        );
    }

    public static SecretBoxException expired(Long secretBoxId) {
        return new SecretBoxException(
            String.format("Secret box %d has expired", secretBoxId),
            ErrorCodes.SECRET_BOX_EXPIRED
        );
    }

    public static SecretBoxException insufficientQuantity(Long secretBoxId, int available) {
        return new SecretBoxException(
            String.format("Insufficient quantity for secret box %d. Only %d available.", secretBoxId, available),
            ErrorCodes.SECRET_BOX_INSUFFICIENT_QUANTITY
        );
    }

    public static SecretBoxException shopNotFoundOrAccessDenied() {
        return new SecretBoxException(
            "Shop not found or you don't have permission",
            ErrorCodes.SHOP_ACCESS_DENIED
        );
    }

    public static SecretBoxException foodItemNotFound(Long foodItemId) {
        return new SecretBoxException(
            String.format("Food item not found or doesn't belong to this shop: %d", foodItemId),
            ErrorCodes.FOOD_ITEM_NOT_FOUND
        );
    }
}
