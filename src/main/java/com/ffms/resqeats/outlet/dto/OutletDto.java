package com.ffms.resqeats.outlet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Outlet response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletDto {

    private Long id;

    @JsonProperty("merchant_id")
    private Long merchantId;

    private String name;

    private String description;

    private String address;

    private String city;

    @JsonProperty("postal_code")
    private String postalCode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String phone;

    private String email;

    @JsonProperty("image_url")
    private String imageUrl;

    private OutletStatus status;

    @JsonProperty("is_open")
    private Boolean isOpen;

    @JsonProperty("average_rating")
    private BigDecimal averageRating;

    @JsonProperty("total_ratings")
    private Integer totalRatings;

    @JsonProperty("operating_hours")
    private List<OperatingHoursDto> operatingHours;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHoursDto {
        @JsonProperty("day_of_week")
        private Integer dayOfWeek;  // 0=Sunday, 6=Saturday

        @JsonProperty("open_time")
        private LocalTime openTime;

        @JsonProperty("close_time")
        private LocalTime closeTime;

        @JsonProperty("is_closed")
        private Boolean isClosed;
    }
}
