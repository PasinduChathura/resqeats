package com.ffms.resqeats.task;

import com.ffms.resqeats.auth.repository.RefreshTokenRepository;
import com.ffms.resqeats.auth.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Task to purge expired tokens.
 * Per SRS Section 4.2 (FR-U-009): Access tokens expire in 15 minutes, refresh tokens in 7-30 days.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokensPurgeTask {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpCodeRepository otpCodeRepository;

    /**
     * Purge expired refresh tokens.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = refreshTokenRepository.deleteAllExpiredBefore(now);
        if (deleted > 0) {
            log.info("Purged {} expired refresh tokens", deleted);
        }
    }

    /**
     * Purge expired OTP codes.
     * Per SRS: OTP codes expire after 5 minutes.
     * Runs every hour.
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void purgeExpiredOtpCodes() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(5);
        int deleted = otpCodeRepository.deleteAllExpiredBefore(expiryTime);
        if (deleted > 0) {
            log.info("Purged {} expired OTP codes", deleted);
        }
    }
}
