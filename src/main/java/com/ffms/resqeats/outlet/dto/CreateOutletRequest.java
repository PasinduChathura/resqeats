package com.ffms.resqeats.outlet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.dto.common.OperatingHoursDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Create outlet request DTO.
 * <p>
 * LOW FIX (Issue #17): Added @Valid for nested object validation.
 */
@Data
@SuperBuilder
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
    @Valid
    private List<OperatingHoursDto> operatingHours;
}
