package com.ffms.resqeats.exception.shop;

import com.ffms.resqeats.exception.common.BaseException;
import com.ffms.resqeats.exception.common.ErrorCodes;
import lombok.Getter;

@Getter
public class ShopException extends BaseException {

    public ShopException(String message, String errorCode) {
        super(message, errorCode, ErrorCodes.CATEGORY_SHOP);
    }

    public ShopException(String message, String errorCode, String details) {
        super(message, errorCode, ErrorCodes.CATEGORY_SHOP, details);
    }

    public ShopException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, ErrorCodes.CATEGORY_SHOP, cause);
    }

    // Legacy constructor for backward compatibility
    public ShopException(String message) {
        super(message, ErrorCodes.SYSTEM_INTERNAL_ERROR, ErrorCodes.CATEGORY_SHOP);
    }

    // ===================== Factory Methods =====================

    public static ShopException notFound(Long shopId) {
        return new ShopException(
            String.format("Shop not found with id: %d", shopId),
            ErrorCodes.SHOP_NOT_FOUND
        );
    }

    public static ShopException accessDenied(Long shopId) {
        return new ShopException(
            String.format("Access denied to shop with id: %d", shopId),
            ErrorCodes.SHOP_ACCESS_DENIED
        );
    }

    public static ShopException notApproved(Long shopId) {
        return new ShopException(
            String.format("Shop %d is not yet approved", shopId),
            ErrorCodes.SHOP_NOT_APPROVED
        );
    }

    public static ShopException suspended(Long shopId) {
        return new ShopException(
            String.format("Shop %d is currently suspended", shopId),
            ErrorCodes.SHOP_SUSPENDED
        );
    }

    public static ShopException closed(Long shopId) {
        return new ShopException(
            String.format("Shop %d is currently closed", shopId),
            ErrorCodes.SHOP_CLOSED
        );
    }

    public static ShopException invalidStatus(String currentStatus, String requiredStatus) {
        return new ShopException(
            String.format("Invalid shop status. Current: %s, Required: %s", currentStatus, requiredStatus),
            ErrorCodes.SHOP_INVALID_STATUS
        );
    }

    public static ShopException ownerNotFound(Long ownerId) {
        return new ShopException(
            String.format("Shop owner not found with id: %d", ownerId),
            ErrorCodes.USER_NOT_FOUND
        );
    }
}
