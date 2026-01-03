package com.ffms.resqeats.service.security.impl;

import com.ffms.resqeats.common.service.CommonServiceImpl;
import com.ffms.resqeats.exception.security.RefreshTokenException;
import com.ffms.resqeats.models.security.RefreshToken;
import com.ffms.resqeats.repository.security.RefreshTokenRepository;
import com.ffms.resqeats.service.security.RefreshTokenService;
import com.ffms.resqeats.service.usermgt.UserService;
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

    @Value("${resqeats.app.refreshTokenExpirationMs}")
    private long tokenExpirationMs;

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
