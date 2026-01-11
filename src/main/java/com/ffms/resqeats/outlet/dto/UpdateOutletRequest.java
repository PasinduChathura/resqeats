package com.ffms.resqeats.outlet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.dto.common.OperatingHoursDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Update outlet request DTO.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOutletRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @Size(max = 500)
    private String address;

    @Size(max = 255)
    private String city;

    @Size(max = 255)
    private String state;

    @Size(max = 50)
    private String postalCode;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private BigDecimal longitude;

    @Size(max = 50)
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 254)
    private String email;

    @Size(max = 2000)
    private String imageUrl;

    @JsonProperty("operating_hours")
    @Valid
    private List<OperatingHoursDto> operatingHours;
}
