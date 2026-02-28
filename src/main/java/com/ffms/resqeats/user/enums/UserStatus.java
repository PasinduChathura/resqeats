package com.ffms.resqeats.user.enums;

/**
 * User account status per SRS Section 7.2.
 */
public enum UserStatus {
    /**
     * User account is active and can use the platform.
     */
    ACTIVE,

    /**
     * User account is disabled (soft deleted or intentionally disabled).
     */
    DISABLED,

    /**
     * User account is suspended due to policy violations.
     */
    SUSPENDED
}
