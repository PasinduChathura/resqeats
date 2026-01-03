package com.ffms.resqeats.controller.auth;

import com.ffms.resqeats.dto.auth.SignupRequest;
import com.ffms.resqeats.dto.security.RefreshTokenDto;
import com.ffms.resqeats.dto.usermgt.password.PasswordChangeDto;
import com.ffms.resqeats.dto.usermgt.password.PasswordResetDto;
import com.ffms.resqeats.dto.usermgt.user.UserLoginDto;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller handling user registration, login, and password management.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignupRequest signupRequest) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(signupRequest));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody UserLoginDto loginRequest) throws Exception {
        return ResponseEntity.ok(authService.signIn(loginRequest));
    }

    /**
     * Get current authenticated user's profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        return ResponseEntity.ok(authService.getCurrentUserProfile(userDetails.getId()));
    }

    @PostMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changeCurrentUserPassword(@Valid @RequestBody PasswordChangeDto passwordDto) throws Exception {
        return ResponseEntity.ok(authService.changeCurrentUserPassword(passwordDto));
    }

    @PostMapping("/resetPassword/mail")
    public ResponseEntity<?> sendResetPasswordMail(final HttpServletRequest request, @RequestParam("email") final String userEmail) throws Exception {
        return ResponseEntity.ok(authService.sendResetPasswordMail(request, userEmail));
    }

    @PostMapping("/me/resetPassword")
    public ResponseEntity<?> resetUserPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) throws Exception {
        return ResponseEntity.ok(authService.resetUserPassword(passwordResetDto));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshJwtToken(@RequestBody RefreshTokenDto refreshTokenDto) throws Exception {
        return ResponseEntity.ok(authService.refreshJwtToken(refreshTokenDto));
    }
}
