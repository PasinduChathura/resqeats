package com.ffms.trackable.security.oauth2;

import com.ffms.trackable.common.model.Status;
import com.ffms.trackable.enums.usermgt.RoleType;
import com.ffms.trackable.enums.usermgt.UserType;
import com.ffms.trackable.models.usermgt.Role;
import com.ffms.trackable.models.usermgt.User;
import com.ffms.trackable.repository.usermgt.RoleRepository;
import com.ffms.trackable.repository.usermgt.UserRepository;
import com.ffms.trackable.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(), 
                oAuth2User.getAttributes()
        );

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user = updateExistingUser(user, oAuth2UserInfo, provider);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo, provider);
        }

        return CustomUserDetails.build(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo, String provider) {
        // Get default USER role
        Optional<Role> defaultRole = roleRepository.findByNameAndType("User", RoleType.USER);
        
        if (defaultRole.isEmpty()) {
            // Fallback: try to get any USER role
            defaultRole = roleRepository.findAllByType(RoleType.USER)
                    .map(roles -> roles.isEmpty() ? null : roles.get(0));
        }

        User user = new User();
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setUserName(oAuth2UserInfo.getEmail());
        user.setFirstName(oAuth2UserInfo.getName());
        user.setStatus(Status.ACTIVE);
        user.setType(UserType.USER);
        user.setOauth2Provider(provider);
        user.setOauth2ProviderId(oAuth2UserInfo.getId());
        
        // Set encoded empty password for OAuth2 users (they don't use password login)
        user.setPassword(passwordEncoder.encode(""));
        
        // Assign default role if found
        defaultRole.ifPresent(user::setRole);

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo, String provider) {
        // Update OAuth2 info if not already set
        if (!StringUtils.hasText(existingUser.getOauth2Provider())) {
            existingUser.setOauth2Provider(provider);
            existingUser.setOauth2ProviderId(oAuth2UserInfo.getId());
        }
        
        // Update name if not set
        if (!StringUtils.hasText(existingUser.getFirstName())) {
            existingUser.setFirstName(oAuth2UserInfo.getName());
        }
        
        return userRepository.save(existingUser);
    }
}
