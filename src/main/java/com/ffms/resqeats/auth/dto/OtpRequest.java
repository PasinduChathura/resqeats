package com.ffms.resqeats.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OTP request DTO per SRS Section 4.1 (FR-M-001).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {

    /**
     * Phone number or email to send OTP to.
     */
    @NotBlank(message = "Phone or email is required")
    private String destination;

    /**
     * Purpose: registration, login, password_reset.
     */
    private String purpose;
}
