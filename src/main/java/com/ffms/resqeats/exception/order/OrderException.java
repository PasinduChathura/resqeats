package com.ffms.resqeats.exception.order;

import com.ffms.resqeats.exception.common.BaseException;
import com.ffms.resqeats.exception.common.ErrorCodes;
import lombok.Getter;

@Getter
public class OrderException extends BaseException {

    public OrderException(String message, String errorCode) {
        super(message, errorCode, ErrorCodes.CATEGORY_ORDER);
    }

    public OrderException(String message, String errorCode, String details) {
        super(message, errorCode, ErrorCodes.CATEGORY_ORDER, details);
    }

    public OrderException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, ErrorCodes.CATEGORY_ORDER, cause);
    }

    // Legacy constructor for backward compatibility
    public OrderException(String message) {
        super(message, ErrorCodes.SYSTEM_INTERNAL_ERROR, ErrorCodes.CATEGORY_ORDER);
    }

    // ===================== Factory Methods =====================

    public static OrderException notFound(Long orderId) {
        return new OrderException(
            String.format("Order not found with id: %d", orderId),
            ErrorCodes.ORDER_NOT_FOUND
        );
    }

    public static OrderException notFoundByNumber(String orderNumber) {
        return new OrderException(
            String.format("Order not found with number: %s", orderNumber),
            ErrorCodes.ORDER_NOT_FOUND
        );
    }

    public static OrderException accessDenied(Long orderId) {
        return new OrderException(
            String.format("Access denied to order: %d", orderId),
            ErrorCodes.ORDER_ACCESS_DENIED
        );
    }

    public static OrderException invalidStatus(String currentStatus, String requiredStatus) {
        return new OrderException(
            String.format("Invalid order status. Current: %s, Required: %s", currentStatus, requiredStatus),
            ErrorCodes.ORDER_INVALID_STATUS
        );
    }

    public static OrderException alreadyCancelled(Long orderId) {
        return new OrderException(
            String.format("Order %d has already been cancelled", orderId),
            ErrorCodes.ORDER_ALREADY_CANCELLED
        );
    }

    public static OrderException cannotCancel(Long orderId, String reason) {
        return new OrderException(
            String.format("Cannot cancel order %d: %s", orderId, reason),
            ErrorCodes.ORDER_CANNOT_CANCEL
        );
    }

    public static OrderException expired(Long orderId) {
        return new OrderException(
            String.format("Order %d has expired", orderId),
            ErrorCodes.ORDER_EXPIRED
        );
    }

    public static OrderException paymentRequired(Long orderId) {
        return new OrderException(
            String.format("Payment is required for order %d", orderId),
            ErrorCodes.ORDER_PAYMENT_REQUIRED
        );
    }

    public static OrderException invalidPickupCode(Long orderId) {
        return new OrderException(
            String.format("Invalid pickup code for order %d", orderId),
            ErrorCodes.ORDER_INVALID_PICKUP_CODE
        );
    }

    public static OrderException alreadyPickedUp(Long orderId) {
        return new OrderException(
            String.format("Order %d has already been picked up", orderId),
            ErrorCodes.ORDER_ALREADY_PICKED_UP
        );
    }

    public static OrderException emptyCart() {
        return new OrderException(
            "Cannot create order: Cart is empty",
            ErrorCodes.CART_EMPTY
        );
    }

    public static OrderException cartNotFound() {
        return new OrderException(
            "No active cart found",
            ErrorCodes.CART_NOT_FOUND
        );
    }

    public static OrderException cartExpired() {
        return new OrderException(
            "Cart has expired. Please add items again.",
            ErrorCodes.CART_EXPIRED
        );
    }

    public static OrderException multipleShops() {
        return new OrderException(
            "Orders from multiple shops are not supported. Please checkout each shop separately.",
            ErrorCodes.ORDER_MULTIPLE_SHOPS
        );
    }

    public static OrderException itemUnavailable(String itemName) {
        return new OrderException(
            String.format("'%s' is no longer available", itemName),
            ErrorCodes.SECRET_BOX_NOT_AVAILABLE
        );
    }

    public static OrderException insufficientQuantity(String itemName, int available) {
        return new OrderException(
            String.format("Insufficient quantity for '%s'. Only %d available.", itemName, available),
            ErrorCodes.SECRET_BOX_INSUFFICIENT_QUANTITY
        );
    }
}
