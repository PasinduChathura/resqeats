package com.ffms.resqeats.service.security;

import com.ffms.resqeats.common.service.CommonService;
import com.ffms.resqeats.enums.security.PasswordResetTokenStatus;
import com.ffms.resqeats.models.security.PasswordResetToken;
import com.ffms.resqeats.models.usermgt.User;

public interface PasswordResetTokenService extends CommonService<PasswordResetToken, Long> {
    PasswordResetTokenStatus validatePasswordResetToken(String token) throws Exception;

    PasswordResetToken findByToken(String token) throws Exception;

    PasswordResetToken findByUser(User user) throws Exception;
}
