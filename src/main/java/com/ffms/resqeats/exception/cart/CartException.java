package com.ffms.resqeats.exception.cart;

import com.ffms.resqeats.exception.common.BaseException;
import com.ffms.resqeats.exception.common.ErrorCodes;
import lombok.Getter;

@Getter
public class CartException extends BaseException {

    public CartException(String message, String errorCode) {
        super(message, errorCode, ErrorCodes.CATEGORY_CART);
    }

    public CartException(String message, String errorCode, String details) {
        super(message, errorCode, ErrorCodes.CATEGORY_CART, details);
    }

    public CartException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, ErrorCodes.CATEGORY_CART, cause);
    }

    // Legacy constructor for backward compatibility
    public CartException(String message) {
        super(message, ErrorCodes.SYSTEM_INTERNAL_ERROR, ErrorCodes.CATEGORY_CART);
    }

    // ===================== Factory Methods =====================

    public static CartException notFound(Long cartId) {
        return new CartException(
            String.format("Cart not found with id: %d", cartId),
            ErrorCodes.CART_NOT_FOUND
        );
    }

    public static CartException noActiveCart() {
        return new CartException(
            "No active cart found",
            ErrorCodes.CART_NOT_FOUND
        );
    }

    public static CartException expired() {
        return new CartException(
            "Cart has expired. Please add items again.",
            ErrorCodes.CART_EXPIRED
        );
    }

    public static CartException empty() {
        return new CartException(
            "Cart is empty",
            ErrorCodes.CART_EMPTY
        );
    }

    public static CartException itemNotFound(Long itemId) {
        return new CartException(
            String.format("Cart item not found with id: %d", itemId),
            ErrorCodes.CART_ITEM_NOT_FOUND
        );
    }

    public static CartException secretBoxNotFound(Long secretBoxId) {
        return new CartException(
            String.format("Secret box not found with id: %d", secretBoxId),
            ErrorCodes.SECRET_BOX_NOT_FOUND
        );
    }

    public static CartException itemUnavailable(String itemName) {
        return new CartException(
            String.format("'%s' is no longer available", itemName),
            ErrorCodes.CART_ITEM_UNAVAILABLE
        );
    }

    public static CartException insufficientQuantity(String itemName, int available) {
        return new CartException(
            String.format("Insufficient quantity for '%s'. Only %d available.", itemName, available),
            ErrorCodes.CART_QUANTITY_EXCEEDED
        );
    }

    public static CartException quantityExceeded(String itemName, int available) {
        return new CartException(
            String.format("Cannot add more of '%s'. Only %d available.", itemName, available),
            ErrorCodes.CART_QUANTITY_EXCEEDED
        );
    }

    public static CartException multipleShops() {
        return new CartException(
            "Cannot add items from different shops to the same cart",
            ErrorCodes.CART_MULTIPLE_SHOPS
        );
    }
}
