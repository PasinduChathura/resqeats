package com.ffms.trackable.service.security.impl;

import com.ffms.trackable.common.service.CommonServiceImpl;
import com.ffms.trackable.exception.security.RefreshTokenException;
import com.ffms.trackable.models.security.RefreshToken;
import com.ffms.trackable.repository.security.RefreshTokenRepository;
import com.ffms.trackable.service.security.RefreshTokenService;
import com.ffms.trackable.service.usermgt.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl extends CommonServiceImpl<RefreshToken, Long, RefreshTokenRepository> implements RefreshTokenService {
    @Autowired
    UserService userService;

    @Value("${trackable.app.refreshTokenExpirationMs}")
    private int tokenExpirationMs;

    @Override
    public RefreshToken createRefreshToken(String username) throws Exception {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userService.findByUserName(username).get())
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(tokenExpirationMs))
                .build();
        return this.create(refreshToken);
    }

    @Override
    public RefreshToken createRefreshTokenForUser(String username) throws Exception {
        RefreshToken token = this.findByUserName(username).orElse(null);
        if (token == null) return this.createRefreshToken(username);
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            this.deleteById(token.getId());
            return this.createRefreshToken(username);
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) throws Exception {
        return this.repository.findByToken(token);
    }

    public Optional<RefreshToken> findByUserName(String username) throws Exception {
        return this.repository.findByUser(userService.findByUserName(username).get());
    }

    public RefreshToken verifyExpiration(RefreshToken token) throws Exception {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            this.repository.delete(token);
            throw new RefreshTokenException("Refresh token is expired!");
        }
        return token;
    }

    @Override
    public String isValid(RefreshToken refreshToken) {
        return null;
    }
}
