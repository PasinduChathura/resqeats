package com.ffms.resqeats.service.auth.impl;

import com.ffms.resqeats.common.dto.MailDto;
import com.ffms.resqeats.common.dto.StandardResponse;
import com.ffms.resqeats.common.logging.AppLogger;
import com.ffms.resqeats.common.model.Status;
import com.ffms.resqeats.dto.auth.AuthResponse;
import com.ffms.resqeats.dto.auth.SignupRequest;
import com.ffms.resqeats.exception.common.NotFoundException;
import com.ffms.resqeats.dto.security.JwtResponse;
import com.ffms.resqeats.dto.security.RefreshTokenDto;
import com.ffms.resqeats.dto.security.RefreshTokenResponse;
import com.ffms.resqeats.dto.usermgt.password.PasswordChangeDto;
import com.ffms.resqeats.dto.usermgt.password.PasswordResetDto;
import com.ffms.resqeats.dto.usermgt.user.UserLoginDto;
import com.ffms.resqeats.enums.security.PasswordResetTokenStatus;
import com.ffms.resqeats.enums.usermgt.RoleType;
import com.ffms.resqeats.enums.usermgt.UserType;
import com.ffms.resqeats.exception.security.RefreshTokenException;
import com.ffms.resqeats.exception.security.TokenExpiredException;
import com.ffms.resqeats.exception.usermgt.InvalidOldPasswordException;
import com.ffms.resqeats.jwt.JwtUtils;
import com.ffms.resqeats.models.security.PasswordResetToken;
import com.ffms.resqeats.models.security.RefreshToken;
import com.ffms.resqeats.models.usermgt.Role;
import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.repository.usermgt.RoleRepository;
import com.ffms.resqeats.repository.usermgt.UserRepository;
import com.ffms.resqeats.security.CustomUserDetails;
import com.ffms.resqeats.service.auth.AuthService;
import com.ffms.resqeats.service.security.PasswordResetTokenService;
import com.ffms.resqeats.service.security.RefreshTokenService;
import com.ffms.resqeats.service.usermgt.UserService;
import com.ffms.resqeats.util.AppUtils;
import com.ffms.resqeats.util.EmailSender;
import jakarta.mail.AuthenticationFailedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AppLogger appLogger = AppLogger.of(log);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmailSender emailSender;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public StandardResponse<?> signUp(SignupRequest signupRequest) throws Exception {
        appLogger.logStart("SIGNUP", "User", signupRequest.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUserName(signupRequest.getUsername())) {
            appLogger.logWarning("SIGNUP", "User", signupRequest.getUsername(), "Username already taken");
            return StandardResponse.error("Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            appLogger.logWarning("SIGNUP", "User", signupRequest.getEmail(), "Email already in use");
            return StandardResponse.error("Email is already in use!");
        }

        // Determine user type and role type (only USER and SHOP_OWNER can self-register)
        UserType userType = signupRequest.getUserType();
        if (userType == null) {
            userType = UserType.USER;
        }
        
        // Validate user type - prevent self-registration as ADMIN or SUPER_ADMIN
        if (userType == UserType.ADMIN || userType == UserType.SUPER_ADMIN) {
            appLogger.logSecurityEvent("UNAUTHORIZED_SIGNUP_ATTEMPT", 
                    "Attempt to register as admin: " + signupRequest.getUsername());
            return StandardResponse.error("Cannot register as administrator!");
        }

        // Map UserType to RoleType
        RoleType roleType = switch (userType) {
            case SHOP_OWNER -> RoleType.SHOP_OWNER;
            case USER -> RoleType.USER;
            default -> RoleType.USER;
        };

        // Find the corresponding role
        Role role = roleRepository.findByType(roleType)
                .orElseThrow(() -> new NotFoundException("Role not found for type: " + roleType));

        // Create new user
        User user = User.builder()
                .userName(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .phone(signupRequest.getPhone())
                .type(userType)
                .status(Status.ACTIVE)
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        // Create JWT token for the new user
        String jwt = jwtUtils.generateJwtTokenByUserName(savedUser.getUserName());
        RefreshToken refreshToken = refreshTokenService.createRefreshTokenForUser(savedUser.getUserName());

        // Build response with roles (include ROLE_ prefix for role-based authorities)
        List<String> authorities = new java.util.ArrayList<>();
        authorities.add("ROLE_" + role.getType().getName());
        if (role.getPrivileges() != null) {
            role.getPrivileges().forEach(p -> authorities.add(p.getName()));
        }

        AuthResponse authResponse = AuthResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUserName())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .phone(savedUser.getPhone())
                .profileImageUrl(savedUser.getProfileImageUrl())
                .userType(savedUser.getType())
                .oauth2Provider(savedUser.getOauth2Provider())
                .roles(authorities)
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours in seconds
                .build();

        return StandardResponse.success(authResponse);
    }

    @Override
    public StandardResponse<?> signIn(UserLoginDto userLoginDto) throws Exception {
        appLogger.logStart("SIGNIN", "User", userLoginDto.getUserName());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginDto.getUserName(), userLoginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            List<String> privileges = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

            RefreshToken refreshToken = refreshTokenService.createRefreshTokenForUser(userDetails.getUsername());

            appLogger.logAuthAttempt(userLoginDto.getUserName(), true);
            appLogger.logSuccess("SIGNIN", "User", userDetails.getId(), 
                    "Roles: " + String.join(", ", privileges));

            return StandardResponse.success(new JwtResponse(jwt, refreshToken.getToken(), 
                    userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), 
                    privileges, userDetails.getRole()));
        } catch (Exception ex) {
            appLogger.logAuthAttempt(userLoginDto.getUserName(), false);
            throw ex;
        }
    }

    @Override
    public StandardResponse<?> getCurrentUserProfile(Long userId) throws Exception {
        User user = userService.findById(userId);
        
        // Build authorities list
        List<String> authorities = new java.util.ArrayList<>();
        if (user.getRole() != null) {
            authorities.add("ROLE_" + user.getRole().getType().getName());
            if (user.getRole().getPrivileges() != null) {
                user.getRole().getPrivileges().forEach(p -> authorities.add(p.getName()));
            }
        }

        AuthResponse profile = AuthResponse.builder()
                .id(user.getId())
                .username(user.getUserName())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .userType(user.getType())
                .oauth2Provider(user.getOauth2Provider())
                .roles(authorities)
                .build();

        return StandardResponse.success(profile);
    }

    @Override
    public StandardResponse<?> refreshJwtToken(RefreshTokenDto refreshTokenDto) throws Exception {
        appLogger.logStart("REFRESH_TOKEN", "RefreshToken");
        
        Optional<RefreshToken> refreshToken = refreshTokenService.findByToken(refreshTokenDto.getToken());

        if (refreshToken.isEmpty()) {
            appLogger.logWarning("REFRESH_TOKEN", "RefreshToken", "Token not found");
            throw new RefreshTokenException("Refresh token not found! Please login again.");
        }

        User user = refreshTokenService.verifyExpiration(refreshToken.get()).getUser();
        String accessToken = jwtUtils.generateJwtTokenByUserName(user.getUserName());
        
        appLogger.logSuccess("REFRESH_TOKEN", "RefreshToken", user.getId(), 
                "New access token generated for user: " + user.getUserName());
        
        return StandardResponse.success(RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenDto.getToken()).build());
    }

    @Override
    public StandardResponse<?> changeCurrentUserPassword(PasswordChangeDto passwordDto) throws Exception {
        appLogger.logStart("CHANGE_PASSWORD", "User");
        
        Object authCtx = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authCtx == "anonymousUser") {
            appLogger.logSecurityEvent("CHANGE_PASSWORD_FAILED", "Anonymous user attempted password change");
            throw new AuthenticationFailedException("Authentication required to change password!");
        }

        final User user = userService.findById(((CustomUserDetails) authCtx).getId());
        if (!userService.checkIfValidOldPassword(user, passwordDto.getOldPassword())) {
            appLogger.logWarning("CHANGE_PASSWORD", "User", user.getId(), "Invalid old password");
            throw new InvalidOldPasswordException("The old password you entered is incorrect. Please try again.");
        }
        user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
        userService.create(user);
        
        appLogger.logSuccess("CHANGE_PASSWORD", "User", user.getId());
        return StandardResponse.success("Password updated successfully!");
    }

    @Override
    public StandardResponse<?> sendResetPasswordMail(final HttpServletRequest request, final String userEmail) throws Exception {
        appLogger.logStart("SEND_PASSWORD_RESET", "User", userEmail);
        
        final Optional<User> user = userService.findByEmail(userEmail);
        if (user.isEmpty()) {
            appLogger.logWarning("SEND_PASSWORD_RESET", "User", userEmail, "Email not found");
            throw new NotFoundException("No account found with email: " + userEmail + ". Please check and try again.");
        }

        // delete previous password reset tokens
        PasswordResetToken prevToken = passwordResetTokenService.findByUser(user.get());
        if (prevToken != null) {
            passwordResetTokenService.deleteById(prevToken.getId());
            appLogger.debug("Deleted previous password reset token for user: {}", userEmail);
        }

        final String token = UUID.randomUUID().toString();
        passwordResetTokenService.create(new PasswordResetToken(token, user.get()));

        // send user password reset email
        this.sendUserResetTokenEmail(AppUtils.getAppUrl(request), token, user.get());
        
        appLogger.logSuccess("SEND_PASSWORD_RESET", "User", user.get().getId(), "Reset email sent");
        return StandardResponse.success("Password reset instructions have been sent to your email!");
    }

    @Override
    public StandardResponse<?> resetUserPassword(PasswordResetDto passwordDto) throws Exception {
        appLogger.logStart("RESET_PASSWORD", "User");
        
        final PasswordResetTokenStatus tokenStatus = passwordResetTokenService.validatePasswordResetToken(passwordDto.getToken());

        if (tokenStatus == PasswordResetTokenStatus.EXPIRED) {
            appLogger.logWarning("RESET_PASSWORD", "PasswordResetToken", "Token expired");
            throw new TokenExpiredException("Password reset link has expired. Please request a new one.");
        }

        if (tokenStatus == PasswordResetTokenStatus.INVALID_TOKEN) {
            appLogger.logSecurityEvent("INVALID_PASSWORD_RESET_TOKEN", "Invalid token used");
            throw new TokenExpiredException("Invalid password reset link. Please request a new one.");
        }

        User user = Optional.ofNullable(passwordResetTokenService.findByToken(passwordDto.getToken()).getUser()).get();
        user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
        userService.create(user);
        
        appLogger.logSuccess("RESET_PASSWORD", "User", user.getId());
        return StandardResponse.success("Password has been reset successfully! You can now login with your new password.");
    }

    private void sendUserResetTokenEmail(final String contextPath, final String token, final User user) throws Exception {
        final String url = contextPath + "/user/changePassword?token=" + token;

        final String from = "resqeats";
        final String subject = "ResqEats Password Reset";
        final String body = "Reset Password Url: " + " \r\n" + url;

        MailDto mailDto = new MailDto();
        mailDto.setSubject(subject);
        mailDto.setBody(body);
        mailDto.setFrom(from);
        mailDto.setTo(EmailSender.commaSeparatedStringToArray(user.getEmail()));
        mailDto.setCc(EmailSender.commaSeparatedStringToArray(null));
        mailDto.setBcc(EmailSender.commaSeparatedStringToArray(null));
        emailSender.sendEmail(mailDto);
    }
}