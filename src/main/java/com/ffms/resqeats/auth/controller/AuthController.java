package com.ffms.resqeats.auth.controller;

import com.ffms.resqeats.auth.dto.*;
import com.ffms.resqeats.auth.service.AuthService;
import com.ffms.resqeats.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller per SRS Section 6.2.
 *
 * Endpoints:
 * POST /auth/register          - New user registration
 * POST /auth/login             - User login
 * POST /auth/otp/request       - Request OTP
 * POST /auth/otp/verify        - Verify OTP
 * POST /auth/token/refresh     - Refresh access token
 * POST /auth/logout            - Logout (invalidate tokens)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and authorization")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user with email/password or phone")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        try {
            AuthResponse response = authService.register(request);
            log.info("Registration successful for email: {}", request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
        } catch (Exception e) {
            log.error("Registration failed for email: {} - Error: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/signin")
    @Operation(summary = "User login", description = "Login with email/phone and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for user: {}", request.getUsername());
        try {
            AuthResponse response = authService.login(request);
            log.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.warn("Login failed for user: {} - Error: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/otp/request")
    @Operation(summary = "Request OTP", description = "Request OTP for phone/email verification")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@Valid @RequestBody OtpRequest request) {
        log.info("OTP request received for destination: {}", request.getDestination());
        try {
            authService.requestOtp(request);
            log.info("OTP sent successfully to: {}", request.getDestination());
            return ResponseEntity.ok(ApiResponse.success(null, "OTP sent successfully"));
        } catch (Exception e) {
            log.error("OTP request failed for destination: {} - Error: {}", request.getDestination(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP and get authentication tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        log.info("OTP verification request received for destination: {}", request.getDestination());
        try {
            AuthResponse response = authService.verifyOtp(request);
            log.info("OTP verified successfully for destination: {}", request.getDestination());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.warn("OTP verification failed for destination: {} - Error: {}", request.getDestination(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        try {
            AuthResponse response = authService.refreshToken(request);
            log.info("Token refreshed successfully");
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.warn("Token refresh failed - Error: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequest request) {
        log.info("Logout request received");
        try {
            authService.logout(request.getRefreshToken());
            log.info("Logout successful");
            return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
        } catch (Exception e) {
            log.error("Logout failed - Error: {}", e.getMessage());
            throw e;
        }
    }
}
