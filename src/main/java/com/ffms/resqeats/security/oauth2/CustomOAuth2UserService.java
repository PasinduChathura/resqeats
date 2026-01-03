package com.ffms.resqeats.security.oauth2;

import com.ffms.resqeats.common.model.Status;
import com.ffms.resqeats.enums.usermgt.RoleType;
import com.ffms.resqeats.enums.usermgt.UserType;
import com.ffms.resqeats.models.usermgt.Role;
import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.repository.usermgt.RoleRepository;
import com.ffms.resqeats.repository.usermgt.UserRepository;
import com.ffms.resqeats.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

/**
 * Custom OAuth2 User Service for Google authentication.
 * Handles user registration and login via Google OAuth2.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    @Transactional
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        
        // Only Google is supported
        if (!"google".equalsIgnoreCase(registrationId)) {
            throw new OAuth2AuthenticationException("Login with " + registrationId + " is not supported");
        }

        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

        if (!StringUtils.hasText(userInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from Google");
        }

        log.info("Processing Google OAuth2 login for: {}", userInfo.getEmail());
        
        // Check if user exists by email
        Optional<User> existingUser = userRepository.findByEmailWithRoleAndPrivileges(userInfo.getEmail());
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Found existing user by email: {}", user.getUserName());
            
            // Update OAuth2 info if not already linked to Google
            if (!StringUtils.hasText(user.getOauth2Provider()) || 
                !"GOOGLE".equalsIgnoreCase(user.getOauth2Provider())) {
                user.setOauth2Provider("GOOGLE");
                user.setOauth2ProviderId(userInfo.getId());
                user = userRepository.save(user);
                log.info("Linked Google account to existing user: {}", user.getUserName());
            }
        } else {
            user = registerNewUser(userInfo);
        }

        return CustomUserDetails.build(user, oAuth2User.getAttributes());
    }

    /**
     * Register a new user from Google OAuth2.
     */
    private User registerNewUser(GoogleOAuth2UserInfo userInfo) {
        Role defaultRole = roleRepository.findByTypeWithPrivileges(RoleType.USER)
                .orElseThrow(() -> new OAuth2AuthenticationException("Default USER role not found"));

        String username = generateUniqueUsername(userInfo.getEmail().split("@")[0]);

        User user = User.builder()
                .userName(username)
                .email(userInfo.getEmail())
                .firstName(StringUtils.hasText(userInfo.getFirstName()) ? userInfo.getFirstName() : userInfo.getName())
                .lastName(userInfo.getLastName())
                .profileImageUrl(userInfo.getImageUrl())
                .oauth2Provider("GOOGLE")
                .oauth2ProviderId(userInfo.getId())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .type(UserType.USER)
                .status(Status.ACTIVE)
                .role(defaultRole)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New Google user registered: {}", savedUser.getUserName());
        
        return savedUser;
    }

    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUserName(username)) {
            username = baseUsername + counter++;
        }
        return username;
    }
}
