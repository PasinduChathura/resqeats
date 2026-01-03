package com.ffms.resqeats.service.auth;

import com.ffms.resqeats.common.dto.StandardResponse;
import com.ffms.resqeats.dto.auth.SignupRequest;
import com.ffms.resqeats.dto.security.RefreshTokenDto;
import com.ffms.resqeats.dto.usermgt.password.PasswordChangeDto;
import com.ffms.resqeats.dto.usermgt.password.PasswordResetDto;
import com.ffms.resqeats.dto.usermgt.user.UserLoginDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    StandardResponse<?> signUp(SignupRequest signupRequest) throws Exception;

    StandardResponse<?> signIn(UserLoginDto userLoginDto) throws Exception;

    StandardResponse<?> getCurrentUserProfile(Long userId) throws Exception;

    StandardResponse<?> refreshJwtToken(RefreshTokenDto refreshTokenDto) throws Exception;

    StandardResponse<?> changeCurrentUserPassword(PasswordChangeDto passwordChangeDto) throws Exception;

    StandardResponse<?> sendResetPasswordMail(final HttpServletRequest request, final String userEmail) throws Exception;

    StandardResponse<?> resetUserPassword(PasswordResetDto passwordResetDto) throws Exception;
}

