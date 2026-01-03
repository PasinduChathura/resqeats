package com.ffms.trackable.service.security;

import com.ffms.trackable.common.service.CommonService;
import com.ffms.trackable.enums.security.PasswordResetTokenStatus;
import com.ffms.trackable.models.security.PasswordResetToken;
import com.ffms.trackable.models.usermgt.User;

public interface PasswordResetTokenService extends CommonService<PasswordResetToken, Long> {
    PasswordResetTokenStatus validatePasswordResetToken(String token) throws Exception;

    PasswordResetToken findByToken(String token) throws Exception;

    PasswordResetToken findByUser(User user) throws Exception;
}
