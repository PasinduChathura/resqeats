package com.ffms.resqeats.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for receiving payment gateway webhook callbacks.
 * This follows the SRS 10.7 Payment Result Webhook specification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookRequest {
    
    /**
     * Unique transaction ID from the payment gateway
     */
    private String transactionId;
    
    /**
     * Our internal order reference (order ID or payment reference)
     */
    private String orderReference;
    
    /**
     * Payment status from gateway: SUCCESS, FAILED, PENDING, CANCELLED
     */
    private String status;
    
    /**
     * The amount that was processed
     */
    private BigDecimal amount;
    
    /**
     * Currency code (e.g., LKR, USD)
     */
    private String currency;
    
    /**
     * Authorization code for successful transactions
     */
    private String authorizationCode;
    
    /**
     * Error code if payment failed
     */
    private String errorCode;
    
    /**
     * Error message if payment failed
     */
    private String errorMessage;
    
    /**
     * Timestamp of the transaction from the gateway
     */
    private String timestamp;
    
    /**
     * Signature/hash for webhook validation (to verify authenticity)
     */
    private String signature;
    
    /**
     * Card type used (VISA, MASTERCARD, etc.) - for card payments
     */
    private String cardType;
    
    /**
     * Last 4 digits of card - for card payments
     */
    private String cardLastFour;
}
