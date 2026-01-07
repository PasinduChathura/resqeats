package com.ffms.resqeats.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Login request DTO per SRS Section 5.1 (FR-W-001).
 * 
 * HIGH-007 FIX: Excluded password from toString() to prevent accidental logging.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class LoginRequest {

    /**
     * Username/email/phone for login.
     */
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
