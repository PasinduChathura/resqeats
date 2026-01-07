package com.ffms.resqeats.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * User registration request per SRS Section 4.1 and 5.1.
 * 
 * HIGH-007 FIX: Excluded password from toString() to prevent accidental logging.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class RegisterRequest {

    /**
     * Phone number for mobile registration (FR-M-001).
     */
    private String phone;

    /**
     * Email for web registration (FR-M-003, FR-W-001).
     */
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Password with complexity requirements per FR-W-001.
     * Min 8 chars, uppercase, lowercase, number, special char.
     */
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;
}
