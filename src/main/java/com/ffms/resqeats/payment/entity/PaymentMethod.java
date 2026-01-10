package com.ffms.resqeats.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * PaymentMethod entity per SRS Section 7.2.
 * Stores tokenized payment methods (no raw card data per PCI-DSS).
 */
@Entity
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_payment_method_user", columnList = "user_id")
})
@FilterDef(name = "paymentMethodUserFilter", parameters = @ParamDef(name = "userId", type = Long.class))
@Filter(name = "paymentMethodUserFilter", condition = "user_id = :userId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private Long userId;

    /**
     * IPG token representing the card.
     */
    @NotNull
    @Column(name = "token", length = 255, nullable = false)
    @JsonProperty("token")
    private String token;

    /**
     * Last 4 digits of card for display.
     */
    @Column(name = "card_last_four", length = 4)
    @JsonProperty("card_last_four")
    private String cardLastFour;

    /**
     * Card brand (Visa, Mastercard, etc.).
     */
    @Column(name = "card_brand", length = 20)
    @JsonProperty("card_brand")
    private String cardBrand;

    /**
     * Card expiration month.
     */
    @Column(name = "expiry_month")
    @JsonProperty("expiry_month")
    private Integer expiryMonth;

    /**
     * Card expiration year.
     */
    @Column(name = "expiry_year")
    @JsonProperty("expiry_year")
    private Integer expiryYear;

    /**
     * Whether this is the user's default payment method.
     */
    @Column(name = "is_default")
    @JsonProperty("is_default")
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * Whether the payment method is active.
     */
    @Column(name = "is_active")
    @JsonProperty("is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Cardholder name (optional).
     */
    @Column(name = "cardholder_name", length = 255)
    @JsonProperty("cardholder_name")
    private String cardholderName;

    /**
     * Get masked card number for display.
     */
    public String getMaskedCardNumber() {
        return "**** **** **** " + (cardLastFour != null ? cardLastFour : "****");
    }

    /**
     * Check if card is expired.
     */
    public boolean isExpired() {
        if (expiryYear == null || expiryMonth == null) {
            return false;
        }
        java.time.YearMonth expiry = java.time.YearMonth.of(expiryYear, expiryMonth);
        return java.time.YearMonth.now().isAfter(expiry);
    }
}
