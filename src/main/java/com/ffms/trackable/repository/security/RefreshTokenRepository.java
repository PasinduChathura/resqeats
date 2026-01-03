package com.ffms.trackable.repository.security;

import com.ffms.trackable.models.security.RefreshToken;
import com.ffms.trackable.models.usermgt.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);
}
