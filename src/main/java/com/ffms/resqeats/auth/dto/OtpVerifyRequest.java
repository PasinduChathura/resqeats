package com.ffms.resqeats.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OTP verification request per SRS Section 4.1 (FR-M-002).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyRequest {

    /**
     * Phone number or email the OTP was sent to.
     */
    @NotBlank(message = "Destination is required")
    private String destination;

    /**
     * 6-digit OTP code.
     */
    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String code;
}
