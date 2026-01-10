package com.ffms.resqeats.auth.service;

import com.ffms.resqeats.auth.entity.RefreshToken;
import com.ffms.resqeats.auth.repository.RefreshTokenRepository;
import com.ffms.resqeats.exception.security.RefreshTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing refresh tokens and user sessions.
 *
 * <p>Provides functionality for:</p>
 * <ul>
 *   <li>Creating refresh tokens for web and mobile clients</li>
 *   <li>Validating and verifying refresh tokens</li>
 *   <li>Revoking tokens for logout operations</li>
 *   <li>Managing concurrent user sessions</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiry-days:7}")
    private int refreshTokenExpiryDays;

    @Value("${app.jwt.mobile-refresh-token-expiry-days:30}")
    private int mobileRefreshTokenExpiryDays;

    /**
     * Creates a new refresh token for a user with default settings.
     *
     * @param userId the user ID to create token for
     * @return the created refresh token entity
     */
    @Transactional
    public RefreshToken createRefreshTokenForUser(Long userId) {
        log.info("Creating refresh token for user: {}", userId);
        return createRefreshTokenForUser(userId, false, null);
    }

    /**
     * Creates a new refresh token for a user with device-specific settings.
     *
     * @param userId the user ID to create token for
     * @param isMobile whether the token is for a mobile device
     * @param deviceInfo optional device information
     * @return the created refresh token entity
     */
    @Transactional
    public RefreshToken createRefreshTokenForUser(Long userId, boolean isMobile, String deviceInfo) {
        log.info("Creating refresh token for user: {}, mobile: {}, device: {}", userId, isMobile, deviceInfo);
        
        int expiryDays = isMobile ? mobileRefreshTokenExpiryDays : refreshTokenExpiryDays;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(expiryDays);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .expiresAt(expiresAt)
                .deviceInfo(deviceInfo)
                .revoked(false)
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created successfully for user: {}, expires: {}", userId, expiresAt);
        return refreshToken;
    }

    /**
     * Verifies a refresh token and returns it if valid.
     *
     * @param token the refresh token string to verify
     * @return the verified refresh token entity
     * @throws RefreshTokenException if the token is invalid, revoked, or expired
     */
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        log.debug("Verifying refresh token");
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token verification failed - token not found");
                    return new RefreshTokenException("Invalid refresh token");
                });

        if (refreshToken.getRevoked()) {
            log.warn("Refresh token verification failed - token revoked for user: {}", refreshToken.getUserId());
            throw new RefreshTokenException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token verification failed - token expired for user: {}", refreshToken.getUserId());
            throw new RefreshTokenException("Refresh token has expired");
        }

        log.info("Refresh token verified successfully for user: {}", refreshToken.getUserId());
        return refreshToken;
    }

    /**
     * Revokes a specific refresh token.
     *
     * @param token the refresh token string to revoke
     * @throws RefreshTokenException if the token is not found
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        log.info("Processing refresh token revocation");
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Token revocation failed - token not found");
                    return new RefreshTokenException("Invalid refresh token");
                });

        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token revoked successfully for user: {}", refreshToken.getUserId());
    }

    /**
     * Revokes all refresh tokens for a user (logout from all devices).
     *
     * @param userId the user ID to revoke all tokens for
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        log.info("Revoking all refresh tokens for user: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
        log.info("All refresh tokens revoked successfully for user: {}", userId);
    }

    /**
     * Counts the number of active sessions for a user.
     *
     * @param userId the user ID to count sessions for
     * @return the number of active sessions
     */
    @Transactional(readOnly = true)
    public long countActiveSessions(Long userId) {
        log.debug("Counting active sessions for user: {}", userId);
        long count = refreshTokenRepository.countActiveSessionsByUserId(userId, LocalDateTime.now());
        log.debug("Active session count for user: {} is {}", userId, count);
        return count;
    }
}
