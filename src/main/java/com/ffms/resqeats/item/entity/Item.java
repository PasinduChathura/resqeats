package com.ffms.resqeats.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import com.ffms.resqeats.item.enums.ItemCategory;
import com.ffms.resqeats.item.enums.ItemStatus;
import com.ffms.resqeats.item.enums.ItemType;
import com.ffms.resqeats.security.tenant.TenantScoped;
import com.ffms.resqeats.security.tenant.TenantScopeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.ParamDef;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Item entity per SRS Section 7.2.
 * Represents items or Secret Boxes created by merchants.
 * Items are created by Merchants and assigned to one or more Outlets.
 * 
 * TENANT SCOPED: Filtered by merchant_id for MERCHANT role.
 */
@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_item_merchant", columnList = "merchant_id"),
        @Index(name = "idx_item_status", columnList = "status"),
        @Index(name = "idx_item_type", columnList = "item_type")
})
@FilterDef(name = "itemMerchantFilter", parameters = @ParamDef(name = "merchantId", type = String.class))
@Filter(name = "itemMerchantFilter", condition = "merchant_id = :merchantId")
@TenantScoped(TenantScopeType.MERCHANT)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends BaseEntity {

    @NotNull
    @Column(name = "merchant_id", nullable = false)
    @JsonProperty("merchant_id")
    private UUID merchantId;

    @NotBlank
    @Column(name = "name", length = 255, nullable = false)
    @JsonProperty("name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    @JsonProperty("category")
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 20, nullable = false)
    @JsonProperty("item_type")
    @Builder.Default
    private ItemType itemType = ItemType.SECRET_BOX;

    /**
     * Original retail value before discount.
     */
    @NotNull
    @Positive
    @Column(name = "base_price", precision = 10, scale = 2, nullable = false)
    @JsonProperty("base_price")
    private BigDecimal basePrice;

    /**
     * Discounted sale price for customers.
     */
    @NotNull
    @Positive
    @Column(name = "sale_price", precision = 10, scale = 2, nullable = false)
    @JsonProperty("sale_price")
    private BigDecimal salePrice;

    @Column(name = "image_url", length = 500)
    @JsonProperty("image_url")
    private String imageUrl;

    /**
     * Dietary information as JSON (vegetarian, vegan, allergens).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dietary_info", columnDefinition = "JSON")
    @JsonProperty("dietary_info")
    private Map<String, Object> dietaryInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @JsonProperty("status")
    @Builder.Default
    private ItemStatus status = ItemStatus.ACTIVE;

    /**
     * Calculate discount percentage.
     */
    public BigDecimal getDiscountPercentage() {
        if (basePrice == null || salePrice == null || basePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return basePrice.subtract(salePrice)
                .divide(basePrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Check if item is active and can be sold.
     */
    public boolean isActive() {
        return status == ItemStatus.ACTIVE;
    }
}
