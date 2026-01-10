package com.ffms.resqeats.security.oauth2;

import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import com.ffms.resqeats.user.repository.UserRepository;
import com.ffms.resqeats.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Custom OAuth2 User Service for handling Google authentication.
 *
 * <p>This service extends Spring Security's DefaultOAuth2UserService to provide
 * custom user loading and registration logic for OAuth2 authentication flows.
 * As specified in SRS Section 4.2 (FR-U-010), this implementation supports
 * social login exclusively via Google.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Automatic user registration for new Google OAuth2 users</li>
 *   <li>Account linking for existing users signing in with Google</li>
 *   <li>Email verification bypass for OAuth2-authenticated users</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * Loads the OAuth2 user from the OAuth2 provider.
     *
     * <p>This method delegates to the parent class to fetch user information
     * from the OAuth2 provider, then processes the user data to either
     * authenticate an existing user or register a new one.</p>
     *
     * @param oAuth2UserRequest the OAuth2 user request containing client registration
     *        and access token information
     * @return the authenticated OAuth2User with application-specific details
     * @throws OAuth2AuthenticationException when authentication fails due to
     *         unsupported provider, missing email, or other OAuth2 errors
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        log.info("Loading OAuth2 user from provider: {}",
                oAuth2UserRequest.getClientRegistration().getRegistrationId());
        log.debug("OAuth2 access token type: {}",
                oAuth2UserRequest.getAccessToken().getTokenType().getValue());

        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        log.debug("Successfully fetched OAuth2 user attributes from provider");

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            log.error("Authentication exception during OAuth2 processing: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during OAuth2 user processing: {}", ex.getMessage(), ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * Processes the OAuth2 user information and manages user registration or linking.
     *
     * <p>This method handles the core logic of OAuth2 authentication:</p>
     * <ul>
     *   <li>Validates that the provider is Google (only supported provider)</li>
     *   <li>Extracts user information from OAuth2 attributes</li>
     *   <li>Finds existing user by email or creates a new user account</li>
     *   <li>Links existing accounts to Google if not already linked</li>
     * </ul>
     *
     * @param oAuth2UserRequest the OAuth2 user request containing registration info
     * @param oAuth2User the OAuth2 user data from the provider
     * @return the processed OAuth2User with application-specific CustomUserDetails
     * @throws OAuth2AuthenticationException when provider is not supported or
     *         required email is missing from the OAuth2 response
     */
    @Transactional
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        log.debug("Processing OAuth2 user for registration: {}", registrationId);

        if (!"google".equalsIgnoreCase(registrationId)) {
            log.warn("Unsupported OAuth2 provider attempted: {}", registrationId);
            throw new OAuth2AuthenticationException("Login with " + registrationId + " is not supported");
        }

        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        log.debug("Extracted Google user info - ID: {}, Name: {} {}",
                userInfo.getId(), userInfo.getFirstName(), userInfo.getLastName());

        if (!StringUtils.hasText(userInfo.getEmail())) {
            log.error("Email not found in Google OAuth2 response for user ID: {}", userInfo.getId());
            throw new OAuth2AuthenticationException("Email not found from Google");
        }

        log.info("Processing Google OAuth2 login for email: {}", userInfo.getEmail());

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Found existing user by email: {} with ID: {}", user.getEmail(), user.getId());

            if (!StringUtils.hasText(user.getOauth2Provider()) ||
                    !"GOOGLE".equalsIgnoreCase(user.getOauth2Provider())) {
                log.debug("Linking existing account {} to Google OAuth2", user.getId());
                user.setOauth2Provider("GOOGLE");
                user.setOauth2ProviderId(userInfo.getId());
                user = userRepository.save(user);
                log.info("Successfully linked existing account {} to Google", user.getId());
            } else {
                log.debug("User {} already linked to Google", user.getId());
            }
        } else {
            log.info("No existing user found for email: {}, creating new account", userInfo.getEmail());
            user = User.builder()
                    .email(userInfo.getEmail())
                    .firstName(userInfo.getFirstName())
                    .lastName(userInfo.getLastName())
                    .profileImageUrl(userInfo.getImageUrl())
                    .oauth2Provider("GOOGLE")
                    .oauth2ProviderId(userInfo.getId())
                    .role(UserRole.CUSTOMER_USER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            user = userRepository.save(user);
            log.info("Created new user from Google OAuth2 - ID: {}, Email: {}", user.getId(), user.getEmail());
        }

        log.debug("Building CustomUserDetails for user: {}", user.getId());
        return CustomUserDetails.build(user, oAuth2User.getAttributes());
    }
}
