package com.ffms.resqeats.outlet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Update outlet request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOutletRequest {

    private String name;

    private String description;

    private String address;

    private String city;

    private String state;

    private String postalCode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String phone;

    private String email;

    private String imageUrl;
}
