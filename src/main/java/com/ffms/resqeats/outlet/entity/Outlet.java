package com.ffms.resqeats.outlet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import com.ffms.resqeats.security.tenant.TenantScoped;
import com.ffms.resqeats.security.tenant.TenantScopeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;

/**
 * Outlet entity per SRS Section 7.2.
 * Represents a physical location belonging to a merchant where customers pick up orders.
 * Hierarchy: Merchant → Outlet → Item
 * 
 * TENANT SCOPED: Filtered by merchant_id for MERCHANT role.
 */
@Entity
@Table(name = "outlets", indexes = {
        @Index(name = "idx_outlet_merchant", columnList = "merchant_id"),
        @Index(name = "idx_outlet_status", columnList = "status"),
        @Index(name = "idx_outlet_location", columnList = "latitude, longitude")
})
@FilterDef(name = "outletMerchantFilter", parameters = @ParamDef(name = "merchantId", type = Long.class))
@Filter(name = "outletMerchantFilter", condition = "merchant_id = :merchantId")
@TenantScoped(TenantScopeType.MERCHANT)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outlet extends BaseEntity {

    @NotNull
    @Column(name = "merchant_id", nullable = false)
    @JsonProperty("merchant_id")
    private Long merchantId;

    @NotBlank
    @Column(name = "name", length = 255, nullable = false)
    @JsonProperty("name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @NotBlank
    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    @JsonProperty("address")
    private String address;

    @Column(name = "city", length = 100)
    @JsonProperty("city")
    private String city;

    @Column(name = "postal_code", length = 20)
    @JsonProperty("postal_code")
    private String postalCode;

    @NotNull
    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    @JsonProperty("latitude")
    private BigDecimal latitude;

    @NotNull
    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    @JsonProperty("longitude")
    private BigDecimal longitude;

    @Column(name = "phone", length = 20)
    @JsonProperty("phone")
    private String phone;

    @Column(name = "email", length = 255)
    @JsonProperty("email")
    private String email;

    @Column(name = "image_url", length = 500)
    @JsonProperty("image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    @JsonProperty("status")
    @Builder.Default
    private OutletStatus status = OutletStatus.PENDING_APPROVAL;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @JsonProperty("average_rating")
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_ratings")
    @JsonProperty("total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    /**
     * Check if outlet is active and can accept orders.
     * BR-017: Outlets must be approved before appearing in customer app.
     */
    public boolean isActive() {
        return status == OutletStatus.ACTIVE;
    }

    /**
     * Check if outlet is currently accepting orders.
     */
    public boolean canAcceptOrders() {
        return status == OutletStatus.ACTIVE;
    }
}
