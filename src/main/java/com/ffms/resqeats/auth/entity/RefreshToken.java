package com.ffms.resqeats.auth.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh token entity per SRS Section 4.1 (FR-M-004).
 * Supports 30-day expiry for mobile, 7-day for web.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_user", columnList = "user_id"),
        @Index(name = "idx_refresh_token_token", columnList = "token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @NotNull
    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private UUID userId;

    @NotNull
    @Column(name = "token", length = 500, nullable = false, unique = true)
    @JsonProperty("token")
    private String token;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked")
    @JsonProperty("revoked")
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    @JsonProperty("revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Device/client info for session tracking.
     */
    @Column(name = "device_info", length = 255)
    @JsonProperty("device_info")
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    @JsonProperty("ip_address")
    private String ipAddress;

    /**
     * Check if token is expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if token is valid (not expired and not revoked).
     */
    public boolean isValid() {
        return !isExpired() && !Boolean.TRUE.equals(revoked);
    }
}
