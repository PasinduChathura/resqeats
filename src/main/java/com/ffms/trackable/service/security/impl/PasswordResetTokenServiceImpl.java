package com.ffms.trackable.service.security.impl;

import com.ffms.trackable.common.service.CommonServiceImpl;
import com.ffms.trackable.enums.security.PasswordResetTokenStatus;
import com.ffms.trackable.models.security.PasswordResetToken;
import com.ffms.trackable.models.usermgt.User;
import com.ffms.trackable.repository.security.PasswordResetTokenRepository;
import com.ffms.trackable.service.security.PasswordResetTokenService;
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
