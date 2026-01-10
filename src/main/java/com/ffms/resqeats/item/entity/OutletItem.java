package com.ffms.resqeats.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import com.ffms.resqeats.security.tenant.TenantScoped;
import com.ffms.resqeats.security.tenant.TenantScopeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * OutletItem entity per SRS Section 7.2.
 * Junction table for Item-Outlet many-to-many relationship.
 * Allows per-outlet availability and quantity management.
 * 
 * TENANT SCOPED: Filtered by outlet_id for OUTLET_USER role.
 */
@Entity
@Table(name = "outlet_items", indexes = {
        @Index(name = "idx_outlet_item_outlet", columnList = "outlet_id"),
        @Index(name = "idx_outlet_item_item", columnList = "item_id")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"outlet_id", "item_id"})
})
@FilterDef(name = "outletItemOutletFilter", parameters = @ParamDef(name = "outletId", type = Long.class))
@Filter(name = "outletItemOutletFilter", condition = "outlet_id = :outletId")
@TenantScoped(TenantScopeType.OUTLET)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutletItem extends BaseEntity {

    @NotNull
    @Column(name = "outlet_id", nullable = false)
    @JsonProperty("outlet_id")
    private Long outletId;

    @NotNull
    @Column(name = "item_id", nullable = false)
    @JsonProperty("item_id")
    private Long itemId;

    /**
     * Whether item is currently available for sale at this outlet.
     */
    @Column(name = "is_available")
    @JsonProperty("is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    /**
     * Default daily quantity reset value.
     */
    @Column(name = "daily_quantity")
    @JsonProperty("daily_quantity")
    @Builder.Default
    private Integer dailyQuantity = 0;

    /**
     * Current available quantity (managed by Redis for real-time).
     * This DB field stores the persisted state.
     */
    @Column(name = "current_quantity")
    @JsonProperty("current_quantity")
    @Builder.Default
    private Integer currentQuantity = 0;

    /**
     * Check if item is available and has stock.
     */
    public boolean isAvailableForPurchase() {
        return Boolean.TRUE.equals(isAvailable) && currentQuantity != null && currentQuantity > 0;
    }
}
