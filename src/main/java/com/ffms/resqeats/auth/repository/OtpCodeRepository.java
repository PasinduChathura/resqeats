package com.ffms.resqeats.auth.repository;

import com.ffms.resqeats.auth.entity.OtpCode;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OTP code repository per SRS Section 6.2.
 */
@Repository
public interface OtpCodeRepository extends com.ffms.resqeats.common.repository.BaseScopedRepository<OtpCode> {

    @Query("SELECT o FROM OtpCode o WHERE o.phone = :phone AND o.verified = false " +
           "AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<OtpCode> findLatestByPhone(@Param("phone") String phone, @Param("now") LocalDateTime now);

    @Query("SELECT o FROM OtpCode o WHERE o.email = :email AND o.verified = false " +
           "AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<OtpCode> findLatestByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.phone = :phone AND o.createdAt > :since")
    long countRecentByPhone(@Param("phone") String phone, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.email = :email AND o.createdAt > :since")
    long countRecentByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now OR o.verified = true")
    void deleteExpiredAndVerifiedCodes(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    int deleteAllExpiredBefore(@Param("now") LocalDateTime now);

    @Override
    default void validateScope(OtpCode entity) {
        // OTP codes are not directly tenant sensitive; validation is performed at service-level
    }
}
