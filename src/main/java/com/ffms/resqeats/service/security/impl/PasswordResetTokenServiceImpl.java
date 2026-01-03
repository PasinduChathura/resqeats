package com.ffms.resqeats.service.security.impl;

import com.ffms.resqeats.common.service.CommonServiceImpl;
import com.ffms.resqeats.enums.security.PasswordResetTokenStatus;
import com.ffms.resqeats.models.security.PasswordResetToken;
import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.repository.security.PasswordResetTokenRepository;
import com.ffms.resqeats.service.security.PasswordResetTokenService;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class PasswordResetTokenServiceImpl extends CommonServiceImpl<PasswordResetToken, Long, PasswordResetTokenRepository> implements PasswordResetTokenService {
    @Override
    public String isValid(PasswordResetToken passwordResetToken) {
        return null;
    }

    @Override
    public PasswordResetTokenStatus validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = this.repository.findByToken(token);

        return !isTokenFound(passToken) ? PasswordResetTokenStatus.INVALID_TOKEN
                : isTokenExpired(passToken) ? PasswordResetTokenStatus.EXPIRED
                : PasswordResetTokenStatus.VALID;
    }

    @Override
    public PasswordResetToken findByToken(String token) {
        return this.repository.findByToken(token);
    }

    @Override
    public PasswordResetToken findByUser(User user) throws Exception {
        return this.repository.findByUser(user);
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }
}
