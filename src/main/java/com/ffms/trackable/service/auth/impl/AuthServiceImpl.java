package com.ffms.trackable.service.auth.impl;

import com.ffms.trackable.common.dto.MailDto;
import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.exception.common.NotFoundException;
import com.ffms.trackable.dto.security.JwtResponse;
import com.ffms.trackable.dto.security.RefreshTokenDto;
import com.ffms.trackable.dto.security.RefreshTokenResponse;
import com.ffms.trackable.dto.usermgt.password.PasswordChangeDto;
import com.ffms.trackable.dto.usermgt.password.PasswordResetDto;
import com.ffms.trackable.dto.usermgt.user.UserLoginDto;
import com.ffms.trackable.enums.security.PasswordResetTokenStatus;
import com.ffms.trackable.exception.security.RefreshTokenException;
import com.ffms.trackable.exception.security.TokenExpiredException;
import com.ffms.trackable.exception.usermgt.InvalidOldPasswordException;
import com.ffms.trackable.jwt.JwtUtils;
import com.ffms.trackable.models.security.PasswordResetToken;
import com.ffms.trackable.models.security.RefreshToken;
import com.ffms.trackable.models.usermgt.User;
import com.ffms.trackable.security.CustomUserDetails;
import com.ffms.trackable.service.auth.AuthService;
import com.ffms.trackable.service.security.PasswordResetTokenService;
import com.ffms.trackable.service.security.RefreshTokenService;
import com.ffms.trackable.service.usermgt.UserService;
import com.ffms.trackable.util.AppUtils;
import com.ffms.trackable.util.EmailSender;
import jakarta.mail.AuthenticationFailedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
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

    @Override
    public StandardResponse<?> signIn(UserLoginDto userLoginDto) throws Exception {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getUserName(), userLoginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> privileges = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshTokenForUser(userDetails.getUsername());

        return StandardResponse.success(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), privileges, userDetails.getRole()));
    }

    @Override
    public StandardResponse<?> refreshJwtToken(RefreshTokenDto refreshTokenDto) throws Exception {
        Optional<RefreshToken> refreshToken = refreshTokenService.findByToken(refreshTokenDto.getToken());

        if (refreshToken.isEmpty()) throw new RefreshTokenException("Refresh token not found!");

        User user = refreshTokenService.verifyExpiration(refreshToken.get()).getUser();
        String accessToken = jwtUtils.generateJwtTokenByUserName(user.getUserName());
        return StandardResponse.success(RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenDto.getToken()).build());
    }

    @Override
    public StandardResponse<?> changeCurrentUserPassword(PasswordChangeDto passwordDto) throws Exception {
        Object authCtx = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authCtx == "anonymousUser") throw new AuthenticationFailedException("Authentication failed!");

        final User user = userService.findById(((CustomUserDetails) authCtx).getId());
        if (!userService.checkIfValidOldPassword(user, passwordDto.getOldPassword())) {
            throw new InvalidOldPasswordException("Entered old password is Invalid");
        }
        user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
        userService.create(user);
        return StandardResponse.success("Password updated successfully!");
    }

    @Override
    public StandardResponse<?> sendResetPasswordMail(final HttpServletRequest request, final String userEmail) throws Exception {
        final Optional<User> user = userService.findByEmail(userEmail);
        if (user.isEmpty())
            throw new NotFoundException("Entered email (" + userEmail + ") doesn't match with the registered email!");

        // delete previous password reset tokens
        PasswordResetToken prevToken = passwordResetTokenService.findByUser(user.get());
        if (prevToken != null) passwordResetTokenService.deleteById(prevToken.getId());

        final String token = UUID.randomUUID().toString();
        passwordResetTokenService.create(new PasswordResetToken(token, user.get()));

        // send user password reset email
        this.sendUserResetTokenEmail(AppUtils.getAppUrl(request), token, user.get());
        return StandardResponse.success("Password reset mail sent your email!");
    }

    @Override
    public StandardResponse<?> resetUserPassword(PasswordResetDto passwordDto) throws Exception {
        final PasswordResetTokenStatus tokenStatus = passwordResetTokenService.validatePasswordResetToken(passwordDto.getToken());

        if (tokenStatus == PasswordResetTokenStatus.EXPIRED)
            throw new TokenExpiredException("Password reset token has expired!");

        if (tokenStatus == PasswordResetTokenStatus.INVALID_TOKEN)
            throw new TokenExpiredException("Invalid password reset token!");

        User user = Optional.ofNullable(passwordResetTokenService.findByToken(passwordDto.getToken()).getUser()).get();
        user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
        userService.create(user);
        return StandardResponse.success("Password reset successfully!");
    }

    private void sendUserResetTokenEmail(final String contextPath, final String token, final User user) throws Exception {
        // add password reset page url endpoint to this
        final String url = contextPath + "/user/changePassword?token=" + token;

        final String from = "Trackable";
        final String subject = "Trackable Password Reset";
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