package com.ffms.resqeats.auth.service;

import com.ffms.resqeats.auth.dto.*;
import com.ffms.resqeats.auth.entity.OtpCode;
import com.ffms.resqeats.auth.entity.RefreshToken;
import com.ffms.resqeats.auth.repository.OtpCodeRepository;
import com.ffms.resqeats.auth.repository.RefreshTokenRepository;
import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.config.JwtConfig;
import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import com.ffms.resqeats.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Authentication service handling user login, registration, OTP verification, and token management.
 *
 * <p>Provides core authentication functionality including:</p>
 * <ul>
 *   <li>Email/password and phone/OTP based authentication</li>
 *   <li>User registration with validation</li>
 *   <li>JWT access token and refresh token generation</li>
 *   <li>Token refresh and session management</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int OTP_RATE_LIMIT_MINUTES = 15;
    private static final int OTP_MAX_REQUESTS = 3;
    private static final int MAX_CONCURRENT_SESSIONS = 3;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    /**
     * Authenticates a user with email/phone and password.
     *
     * @param request the login request containing username and password
     * @return authentication response with tokens and user info
     * @throws BusinessException if credentials are invalid or account is inactive
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for user: {}", request.getUsername());
        
        User user = userRepository.findByEmail(request.getUsername())
                .or(() -> userRepository.findByPhone(request.getUsername()))
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.getUsername());
                    return new BusinessException("AUTH_001", "Invalid credentials");
                });

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login failed - account not active for user: {}, status: {}", user.getId(), user.getStatus());
            throw new BusinessException("AUTH_001", "Account is not active");
        }

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed - invalid password for user: {}", user.getId());
            throw new BusinessException("AUTH_001", "Invalid credentials");
        }

        log.info("Login successful for user: {}, role: {}", user.getId(), user.getRole());
        return generateAuthResponse(user);
    }

    /**
     * Registers a new user account.
     *
     * @param request the registration request with user details
     * @return authentication response with tokens for the new user
     * @throws BusinessException if email or phone already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration request for email: {}", request.getEmail());
        
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new BusinessException("AUTH_001", "Email already registered");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            log.warn("Registration failed - phone already exists: {}", request.getPhone());
            throw new BusinessException("AUTH_001", "Phone number already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(request.getPassword() != null ?
                        passwordEncoder.encode(request.getPassword()) : null)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .phoneVerified(false)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully - userId: {}, email: {}", user.getId(), user.getEmail());

        return generateAuthResponse(user);
    }

    /**
     * Requests an OTP code for the specified destination (phone or email).
     *
     * @param request the OTP request containing destination and purpose
     * @throws BusinessException if rate limit exceeded
     */
    @Transactional
    public void requestOtp(OtpRequest request) {
        String destination = request.getDestination();
        log.info("Processing OTP request for destination: {}", maskDestination(destination));
        
        LocalDateTime now = LocalDateTime.now();
        boolean isPhone = destination.matches("^\\+?[0-9]{10,15}$");
        
        long recentCount = isPhone ?
                otpCodeRepository.countRecentByPhone(destination, now.minusMinutes(OTP_RATE_LIMIT_MINUTES)) :
                otpCodeRepository.countRecentByEmail(destination, now.minusMinutes(OTP_RATE_LIMIT_MINUTES));

        if (recentCount >= OTP_MAX_REQUESTS) {
            log.warn("OTP rate limit exceeded for destination: {}, count: {}", maskDestination(destination), recentCount);
            throw new BusinessException("AUTH_001", "Too many OTP requests. Please wait 15 minutes.");
        }

        String code = String.format("%0" + OTP_LENGTH + "d", SECURE_RANDOM.nextInt((int) Math.pow(10, OTP_LENGTH)));

        OtpCode otp = OtpCode.builder()
                .phone(isPhone ? destination : null)
                .email(isPhone ? null : destination)
                .code(code)
                .expiresAt(now.plusMinutes(OTP_EXPIRY_MINUTES))
                .purpose(request.getPurpose())
                .build();

        otpCodeRepository.save(otp);
        log.info("OTP generated successfully for destination: {}, purpose: {}", maskDestination(destination), request.getPurpose());
        log.debug("OTP code: {} for {}", code, destination);
    }

    /**
     * Verifies an OTP code and authenticates the user.
     *
     * @param request the OTP verification request
     * @return authentication response with tokens
     * @throws BusinessException if OTP is invalid, expired, or max attempts reached
     */
    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        String destination = request.getDestination();
        log.info("Processing OTP verification for destination: {}", maskDestination(destination));
        
        LocalDateTime now = LocalDateTime.now();
        boolean isPhone = destination.matches("^\\+?[0-9]{10,15}$");
        
        OtpCode otp = (isPhone ?
                otpCodeRepository.findLatestByPhone(destination, now) :
                otpCodeRepository.findLatestByEmail(destination, now))
                .orElseThrow(() -> {
                    log.warn("OTP verification failed - OTP not found or expired for: {}", maskDestination(destination));
                    return new BusinessException("AUTH_001", "OTP not found or expired");
                });

        if (!otp.canBeVerified()) {
            if (otp.isMaxAttemptsReached()) {
                log.warn("OTP verification failed - max attempts reached for: {}", maskDestination(destination));
                throw new BusinessException("AUTH_001", "Maximum OTP attempts reached");
            }
            log.warn("OTP verification failed - OTP expired for: {}", maskDestination(destination));
            throw new BusinessException("AUTH_001", "OTP expired");
        }

        if (!otp.getCode().equals(request.getCode())) {
            otp.incrementAttempts();
            otpCodeRepository.save(otp);
            log.warn("OTP verification failed - invalid code for: {}, attempts: {}", maskDestination(destination), otp.getAttempts());
            throw new BusinessException("AUTH_001", "Invalid OTP code");
        }

        otp.setVerified(true);
        otp.setVerifiedAt(now);
        otpCodeRepository.save(otp);

        User user = (isPhone ?
                userRepository.findByPhone(destination) :
                userRepository.findByEmail(destination))
                .orElseGet(() -> {
                    log.info("Creating new user from OTP verification for: {}", maskDestination(destination));
                    User newUser = User.builder()
                            .phone(isPhone ? destination : null)
                            .email(isPhone ? null : destination)
                            .phoneVerified(isPhone)
                            .emailVerified(!isPhone)
                            .role(UserRole.USER)
                            .status(UserStatus.ACTIVE)
                            .build();
                    return userRepository.save(newUser);
                });

        if (isPhone) {
            user.setPhoneVerified(true);
        } else {
            user.setEmailVerified(true);
        }
        userRepository.save(user);

        log.info("OTP verification successful for user: {}", user.getId());
        return generateAuthResponse(user);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param request the refresh token request
     * @return new authentication response with fresh tokens
     * @throws BusinessException if refresh token is invalid or expired
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Processing token refresh request");
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> {
                    log.warn("Token refresh failed - invalid refresh token");
                    return new BusinessException("AUTH_002", "Invalid refresh token");
                });

        if (!refreshToken.isValid()) {
            log.warn("Token refresh failed - refresh token expired or revoked for user: {}", refreshToken.getUserId());
            throw new BusinessException("AUTH_002", "Refresh token expired or revoked");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> {
                    log.error("Token refresh failed - user not found: {}", refreshToken.getUserId());
                    return new BusinessException("AUTH_002", "User not found");
                });

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Token refresh failed - account not active for user: {}", user.getId());
            throw new BusinessException("AUTH_001", "Account is not active");
        }

        log.info("Token refreshed successfully for user: {}", user.getId());
        return generateAuthResponse(user);
    }

    /**
     * Logs out a user by revoking their refresh token.
     *
     * @param refreshToken the refresh token to revoke
     */
    @Transactional
    public void logout(String refreshToken) {
        log.info("Processing logout request");
        
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                    log.info("Logout successful - refresh token revoked for user: {}", token.getUserId());
                });
    }

    /**
     * Logs out a user from all active sessions.
     *
     * @param userId the user ID to logout from all sessions
     */
    @Transactional
    public void logoutAll(UUID userId) {
        log.info("Processing logout-all request for user: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
        log.info("All sessions revoked successfully for user: {}", userId);
    }

    /**
     * Generates authentication response with access and refresh tokens.
     *
     * @param user the authenticated user
     * @return authentication response containing tokens and user details
     */
    private AuthResponse generateAuthResponse(User user) {
        log.debug("Generating auth response for user: {}", user.getId());
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtConfig.getAccessTokenExpirationMs() / 1000)
                .userId(user.getId())
                .role(user.getRole().name())
                .merchantId(user.getMerchantId())
                .outletId(user.getOutletId())
                .build();
    }

    /**
     * Generates a JWT access token for the user.
     *
     * @param user the user for whom to generate the token
     * @return the generated JWT access token
     */
    private String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpirationMs());
        String tokenId = UUID.randomUUID().toString();

        JwtBuilder builder = Jwts.builder()
                .setId(tokenId)
                .setSubject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512);

        if (user.getMerchantId() != null) {
            builder.claim("merchantId", user.getMerchantId().toString());
        }
        if (user.getOutletId() != null) {
            builder.claim("outletId", user.getOutletId().toString());
        }

        log.debug("Generated access token for user: {}, expires: {}", user.getId(), expiry);
        return builder.compact();
    }

    /**
     * Generates a refresh token for the user, managing session limits.
     *
     * @param user the user for whom to generate the token
     * @return the generated refresh token
     */
    private String generateRefreshToken(User user) {
        long activeSessions = refreshTokenRepository.countActiveSessionsByUserId(
                user.getId(), LocalDateTime.now());
        
        if (activeSessions >= MAX_CONCURRENT_SESSIONS) {
            log.info("Max concurrent sessions reached for user: {}, revoking existing sessions", user.getId());
            refreshTokenRepository.revokeAllByUserId(user.getId(), LocalDateTime.now());
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtConfig.getRefreshTokenExpirationDays());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Generated refresh token for user: {}, expires: {}", user.getId(), expiresAt);
        return token;
    }

    /**
     * Validates a JWT token and returns its claims.
     *
     * @param token the JWT token to validate
     * @return the token claims
     */
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID from the token
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Retrieves the signing key for JWT operations.
     *
     * @return the signing key
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtConfig.getSecret())
        );
    }

    /**
     * Masks a destination string for secure logging.
     *
     * @param destination the phone or email to mask
     * @return the masked string
     */
    private String maskDestination(String destination) {
        if (destination == null || destination.length() < 4) {
            return "****";
        }
        return destination.substring(0, 2) + "****" + destination.substring(destination.length() - 2);
    }
}
