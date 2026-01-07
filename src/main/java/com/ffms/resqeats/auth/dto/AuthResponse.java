package com.ffms.resqeats.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Authentication response with JWT tokens per SRS Section 5.1 (FR-W-003).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("role")
    private String role;

    @JsonProperty("merchant_id")
    private UUID merchantId;

    @JsonProperty("outlet_id")
    private UUID outletId;
}
