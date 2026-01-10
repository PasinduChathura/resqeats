package com.ffms.resqeats.jwt;

import com.ffms.resqeats.exception.security.TokenExpiredException;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.user.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * Centralized JWT utility for token generation and validation.
 * 
 * JWT Claims:
 * - sub: userId (Long)
 * - role: UserRole enum name
 * - merchantId: Long (nullable)
 * - outletId: Long (nullable)
 * - email: user email for audit
 * - jti: unique token ID for replay protection
 * - iat: issued at timestamp
 * - exp: expiration timestamp
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${resqeats.app.jwtSecret}")
    private String jwtSecret;

    @Value("${resqeats.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Generate JWT token with full claims from CustomUserDetails.
     */
    public String generateJwtToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        return generateToken(userPrincipal);
    }

    /**
     * Generate JWT token from CustomUserDetails.
     */
    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        String tokenId = UUID.randomUUID().toString();

        JwtBuilder builder = Jwts.builder()
                .setId(tokenId) // jti for replay protection
                .setSubject(userDetails.getId().toString())
                .claim("role", userDetails.getRole())
                .claim("email", userDetails.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key(), SignatureAlgorithm.HS512);

        // Add optional claims
        if (userDetails.getMerchantId() != null) {
            builder.claim("merchantId", userDetails.getMerchantId().toString());
        }
        if (userDetails.getOutletId() != null) {
            builder.claim("outletId", userDetails.getOutletId().toString());
        }

        return builder.compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Get username (user ID) from JWT token.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Parse JWT token and build SecurityContext.
     * This is the ONLY place where JWT is parsed into SecurityContext.
     */
    public ResqeatsSecurityContext parseToken(String token, String correlationId) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.valueOf(claims.getSubject());
        String roleStr = claims.get("role", String.class);
        UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : UserRole.CUSTOMER_USER;

        String merchantIdStr = claims.get("merchantId", String.class);
        Long merchantId = merchantIdStr != null ? Long.valueOf(merchantIdStr) : null;

        String outletIdStr = claims.get("outletId", String.class);
        Long outletId = outletIdStr != null ? Long.valueOf(outletIdStr) : null;

        String email = claims.get("email", String.class);

        return ResqeatsSecurityContext.builder()
                .userId(userId)
                .role(role)
                .merchantId(merchantId)
                .outletId(outletId)
                .email(email)
                .tokenId(claims.getId())
                .issuedAt(claims.getIssuedAt().getTime())
                .expiresAt(claims.getExpiration().getTime())
                .anonymous(false)
                .correlationId(correlationId)
                .build();
    }

    /**
     * Validate JWT token.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new MalformedJwtException("Invalid JWT token!");
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw new TokenExpiredException("JWT token is expired!");
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw new UnsupportedJwtException("JWT token is unsupported!");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw new IllegalArgumentException("JWT claims string is empty!");
        } catch (SignatureException e) {
            logger.error("JWT signature does not match locally computed signature: {}", e.getMessage());
            throw new SignatureException("JWT signature does not match locally computed signature!");
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    /**
     * Get claims from token (for inspection).
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get expiration time in milliseconds.
     */
    public int getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}
