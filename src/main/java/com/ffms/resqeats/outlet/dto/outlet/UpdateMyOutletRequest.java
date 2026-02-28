package com.ffms.resqeats.outlet.dto.outlet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.dto.common.OperatingHoursDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for outlet users to update their assigned outlet.
 * Limited to operational fields only - cannot change name, address, location, or status.
 * 
 * Outlet users can update:
 * - description (customer-facing details)
 * - phone (contact number)
 * - email (contact email)
 * - image_url (outlet photo)
 * - operating_hours (when the outlet is open)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMyOutletRequest {

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    @Size(max = 50, message = "Phone number must be less than 50 characters")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 254, message = "Email must be less than 254 characters")
    private String email;

    @JsonProperty("image_url")
    @Size(max = 2000, message = "Image URL must be less than 2000 characters")
    private String imageUrl;

    @JsonProperty("operating_hours")
    @Valid
    private List<OperatingHoursDto> operatingHours;
}
