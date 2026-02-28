package com.ffms.resqeats.outlet.dto.outlet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.dto.common.OperatingHoursDto;
import com.ffms.resqeats.outlet.enums.OutletAvailabilityStatus;
import com.ffms.resqeats.outlet.enums.OutletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for outlet users to view their assigned outlet.
 * Contains operational fields but excludes admin-only data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletSelfDto {

    private Long id;
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
    private OutletStatus status;

    @JsonProperty("availability_status")
    private OutletAvailabilityStatus availabilityStatus;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_open")
    private Boolean isOpen;

    @JsonProperty("average_rating")
    private BigDecimal averageRating;

    @JsonProperty("total_ratings")
    private Integer totalRatings;

    @JsonProperty("operating_hours")
    private List<OperatingHoursDto> operatingHours;
}
