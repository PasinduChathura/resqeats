package com.ffms.resqeats.dto.shop;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.enums.shop.ShopCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShopRequest {

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

    @JsonProperty("opening_time")
    private LocalTime openingTime;

    @JsonProperty("closing_time")
    private LocalTime closingTime;

    @JsonProperty("pickup_start_time")
    private LocalTime pickupStartTime;

    @JsonProperty("pickup_end_time")
    private LocalTime pickupEndTime;

    @JsonProperty("operating_days")
    private List<OperatingDayRequest> operatingDays;
}
