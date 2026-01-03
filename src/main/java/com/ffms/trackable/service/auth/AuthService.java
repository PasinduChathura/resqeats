package com.ffms.trackable.service.auth;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.dto.security.RefreshTokenDto;
import com.ffms.trackable.dto.usermgt.password.PasswordChangeDto;
import com.ffms.trackable.dto.usermgt.password.PasswordResetDto;
import com.ffms.trackable.dto.usermgt.user.UserLoginDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    StandardResponse<?> signIn(UserLoginDto userLoginDto) throws Exception;

    StandardResponse<?> refreshJwtToken(RefreshTokenDto refreshTokenDto) throws Exception;

    StandardResponse<?> changeCurrentUserPassword(PasswordChangeDto passwordChangeDto) throws Exception;

    StandardResponse<?> sendResetPasswordMail(final HttpServletRequest request, final String userEmail) throws Exception;

    StandardResponse<?> resetUserPassword(PasswordResetDto passwordResetDto) throws Exception;
}

