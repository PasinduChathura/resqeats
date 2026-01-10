package com.ffms.resqeats.outlet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalTime;

/**
 * Outlet operating hours per SRS Section 7.2 (Outlet_Hours entity).
 * Defines daily schedule including pickup windows.
 */
@Entity
@Table(name = "outlet_hours", indexes = {
        @Index(name = "idx_outlet_hours_outlet", columnList = "outlet_id")
})
@FilterDef(name = "outletHoursOutletFilter", parameters = @ParamDef(name = "outletId", type = Long.class))
@Filter(name = "outletHoursOutletFilter", condition = "outlet_id = :outletId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutletHours extends BaseEntity {

    @NotNull
    @Column(name = "outlet_id", nullable = false)
    @JsonProperty("outlet_id")
    private Long outletId;

    /**
     * Day of week: 0=Sunday, 6=Saturday
     */
    @NotNull
    @Column(name = "day_of_week", nullable = false)
    @JsonProperty("day_of_week")
    private Integer dayOfWeek;

    @Column(name = "open_time")
    @JsonProperty("open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    @JsonProperty("close_time")
    private LocalTime closeTime;

    @Column(name = "pickup_start")
    @JsonProperty("pickup_start")
    private LocalTime pickupStart;

    @Column(name = "pickup_end")
    @JsonProperty("pickup_end")
    private LocalTime pickupEnd;

    /**
     * Whether outlet is closed on this day.
     */
    @Column(name = "is_closed")
    @JsonProperty("is_closed")
    @Builder.Default
    private Boolean isClosed = false;

    /**
     * Check if outlet is open at a given time.
     */
    public boolean isOpenAt(LocalTime time) {
        if (Boolean.TRUE.equals(isClosed) || openTime == null || closeTime == null) {
            return false;
        }
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }

    /**
     * Check if pickup is available at a given time.
     */
    public boolean isPickupAvailableAt(LocalTime time) {
        if (Boolean.TRUE.equals(isClosed) || pickupStart == null || pickupEnd == null) {
            return false;
        }
        return !time.isBefore(pickupStart) && !time.isAfter(pickupEnd);
    }
}
