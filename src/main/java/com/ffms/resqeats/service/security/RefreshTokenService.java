package com.ffms.resqeats.service.security;


import com.ffms.resqeats.common.service.CommonService;
import com.ffms.resqeats.models.security.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService extends CommonService<RefreshToken, Long> {
    RefreshToken createRefreshToken(String username) throws Exception;

    RefreshToken createRefreshTokenForUser(String username) throws Exception;

    Optional<RefreshToken> findByToken(String token) throws Exception;

    Optional<RefreshToken> findByUserName(String username) throws Exception;

    RefreshToken verifyExpiration(RefreshToken token) throws Exception;
}
