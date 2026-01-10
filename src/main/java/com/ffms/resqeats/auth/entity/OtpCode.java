package com.ffms.resqeats.auth.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.resqeats.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OTP entity per SRS Section 4.1 (FR-M-001, FR-M-002).
 * 6-digit code, 5-minute expiry, max 3 attempts.
 */
@Entity
@Table(name = "otp_codes", indexes = {
        @Index(name = "idx_otp_phone", columnList = "phone"),
        @Index(name = "idx_otp_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode extends BaseEntity {

    @Column(name = "phone", length = 20)
    @JsonProperty("phone")
    private String phone;

    @Column(name = "email", length = 255)
    @JsonProperty("email")
    private String email;

    @NotNull
    @Column(name = "code", length = 6, nullable = false)
    @JsonProperty("code")
    private String code;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "attempts")
    @JsonProperty("attempts")
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "verified")
    @JsonProperty("verified")
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "verified_at")
    @JsonProperty("verified_at")
    private LocalDateTime verifiedAt;

    /**
     * Purpose of OTP (registration, login, password_reset).
     */
    @Column(name = "purpose", length = 30)
    @JsonProperty("purpose")
    private String purpose;

    /**
     * User ID if this is for an existing user.
     */
    @Column(name = "user_id")
    @JsonProperty("user_id")
    private Long userId;

    /**
     * Check if OTP is expired (5 minutes per SRS).
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if max attempts reached (3 per SRS).
     */
    public boolean isMaxAttemptsReached() {
        return attempts >= 3;
    }

    /**
     * Increment attempt counter.
     */
    public void incrementAttempts() {
        this.attempts = (this.attempts == null ? 0 : this.attempts) + 1;
    }

    /**
     * Check if OTP can still be verified.
     */
    public boolean canBeVerified() {
        return !isExpired() && !isMaxAttemptsReached() && !Boolean.TRUE.equals(verified);
    }
}
