package com.ffms.resqeats.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT configuration per SRS Section 4.1 and 5.1.
 */
@Configuration
@ConfigurationProperties(prefix = "resqeats.app")
@Data
public class JwtConfig {

    /**
     * JWT secret key (should be 512 bits for HS512).
     */
    private String jwtSecret;  // Maps to resqeats.app.jwtSecret

    /**
     * Access token expiration in milliseconds.
     */
    private long jwtExpirationMs = 900000;

    /**
     * Refresh token expiration in milliseconds.
     */
    private long refreshTokenExpirationMs = 2592000000L; // 30 days

    // Update getter names to match what AuthService expects
    public String getSecret() {
        return jwtSecret;
    }

    public long getAccessTokenExpirationMs() {
        return jwtExpirationMs;
    }

    public int getRefreshTokenExpirationDays() {
        return (int) (refreshTokenExpirationMs / (1000 * 60 * 60 * 24));
    }
}