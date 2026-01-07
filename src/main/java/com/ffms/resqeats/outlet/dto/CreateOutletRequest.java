package com.ffms.resqeats.outlet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * Create outlet request DTO.
 * 
 * LOW FIX (Issue #17): Added @Valid for nested object validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutletRequest {

    @NotBlank(message = "Outlet name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @JsonProperty("postal_code")
    private String postalCode;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    private String phone;

    private String email;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("operating_hours")
    @Valid  // LOW FIX: Enables validation of nested OperatingHoursRequest objects
    private List<OperatingHoursRequest> operatingHours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHoursRequest {
        @NotNull(message = "Day of week is required")
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
