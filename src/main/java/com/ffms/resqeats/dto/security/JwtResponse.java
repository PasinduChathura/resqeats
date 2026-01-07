package com.ffms.resqeats.dto.security;

import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * JWT response DTO per SRS Section 4.2 (FR-U-009).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String token;

    private String refreshToken;

    @Builder.Default
    private String type = "Bearer";

    private UUID id;

    private String username;

    private String email;

    private List<String> authorities;

    private String role;

    public JwtResponse(String token, String refreshToken, UUID id, String username, String email, List<String> authorities, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.authorities = authorities;
        this.role = role;
    }
}
