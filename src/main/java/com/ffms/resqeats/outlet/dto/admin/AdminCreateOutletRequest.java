package com.ffms.resqeats.outlet.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.outlet.dto.CreateOutletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Admin create outlet request DTO.
 * Used by ADMIN to create outlets for any merchant.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminCreateOutletRequest extends CreateOutletRequest {

    @NotNull(message = "Merchant ID is required")
    @JsonProperty("merchant_id")
    private Long merchantId;
}
