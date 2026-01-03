package com.ffms.trackable.controller.auth;

import com.ffms.trackable.dto.security.RefreshTokenDto;
import com.ffms.trackable.dto.usermgt.password.PasswordChangeDto;
import com.ffms.trackable.dto.usermgt.password.PasswordResetDto;
import com.ffms.trackable.dto.usermgt.user.UserLoginDto;
import com.ffms.trackable.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody UserLoginDto loginRequest) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(authService.signIn(loginRequest));
    }

    @PostMapping("/me/password")
    public ResponseEntity<?> changeCurrentUserPassword(@Valid @RequestBody PasswordChangeDto passwordDto) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(authService.changeCurrentUserPassword(passwordDto));
    }

    @PostMapping("/resetPassword/mail")
    public ResponseEntity<?> sendResetPasswordMail(final HttpServletRequest request, @RequestParam("email") final String userEmail) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(authService.sendResetPasswordMail(request, userEmail));
    }

    @PostMapping("/me/resetPassword")
    public ResponseEntity<?> resetUserPassword(final Locale locale, @Valid @RequestBody PasswordResetDto passwordResetDto) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(authService.resetUserPassword(passwordResetDto));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshJwtToken(@RequestBody RefreshTokenDto refreshTokenDto) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(authService.refreshJwtToken(refreshTokenDto));
    }
}
