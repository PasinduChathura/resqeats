package com.ffms.resqeats.outlet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outlet list response DTO with merchant association for table display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletListResponseDto {

    private Long id;

    @JsonProperty("merchant_id")
    private Long merchantId;

    private String name;

    private String address;

    private String city;

    @JsonProperty("postal_code")
    private String postalCode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String phone;

    @JsonProperty("image_url")
    private String imageUrl;

    private OutletStatus status;

    @JsonProperty("is_open")
    private Boolean isOpen;

    @JsonProperty("average_rating")
    private BigDecimal averageRating;

    @JsonProperty("total_ratings")
    private Integer totalRatings;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Merchant association data
    @JsonProperty("merchant_name")
    private String merchantName;

    @JsonProperty("merchant_logo_url")
    private String merchantLogoUrl;

    @JsonProperty("merchant_category")
    private String merchantCategory;
}
