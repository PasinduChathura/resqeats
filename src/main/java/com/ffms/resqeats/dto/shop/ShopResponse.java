package com.ffms.resqeats.dto.shop;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.enums.shop.ShopCategory;
import com.ffms.resqeats.enums.shop.ShopStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("address")
    private String address;

    @JsonProperty("city")
    private String city;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("category")
    private ShopCategory category;

    @JsonProperty("status")
    private ShopStatus status;

    @JsonProperty("opening_time")
    private LocalTime openingTime;

    @JsonProperty("closing_time")
    private LocalTime closingTime;

    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;

    @JsonProperty("average_rating")
    private BigDecimal averageRating;

    @JsonProperty("total_ratings")
    private Integer totalRatings;

    @JsonProperty("is_open")
    private Boolean isOpen;

    @JsonProperty("owner_id")
    private Long ownerId;

    @JsonProperty("owner_name")
    private String ownerName;

    @JsonProperty("distance_km")
    private Double distanceKm;

    @JsonProperty("operating_days")
    private List<OperatingDayResponse> operatingDays;
}
