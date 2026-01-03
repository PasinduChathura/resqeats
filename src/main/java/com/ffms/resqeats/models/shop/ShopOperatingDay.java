package com.ffms.resqeats.models.shop;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.model.AuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "shop_operating_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopOperatingDay extends AuditEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", columnDefinition = "VARCHAR(15)", nullable = false)
    @JsonProperty("day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "opening_time")
    @JsonProperty("opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    @JsonProperty("closing_time")
    private LocalTime closingTime;

    @Column(name = "pickup_start_time")
    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @Column(name = "pickup_end_time")
    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;

    @Column(name = "is_closed")
    @JsonProperty("is_closed")
    @Builder.Default
    private Boolean isClosed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonBackReference
    private Shop shop;
}
