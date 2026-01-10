package com.ffms.resqeats.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.repository.UserRepository;

import java.util.Optional;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 *
 * <p>This service is responsible for loading user-specific data during authentication.
 * It supports multiple identification methods including numeric user ID, email, and phone number
 * as specified in SRS Section 6.2. The service also handles OAuth2 provider validation
 * to ensure users authenticate through the correct method.</p>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by username for authentication.
     *
     * <p>This method attempts to find a user by multiple identifiers in the following order:
    * numeric user ID (for JWT token validation), email, and phone number. If the user is registered
     * via OAuth2 provider and has no password set, authentication is rejected with an
     * appropriate message.</p>
     *
     * @param username the username identifying the user (can be UUID, email, or phone)
     * @return the fully populated UserDetails object for the authenticated user
     * @throws UsernameNotFoundException when no user is found with the given identifier
     *         or when OAuth2 user attempts password-based login
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user by username: {}", username);
        log.debug("Searching for user - trying numeric ID, then email, then phone");

        User user = tryLoadById(username)
                .orElseGet(() -> {
                    log.debug("Username is not a valid numeric ID, trying email lookup");
                    return userRepository.findByEmail(username)
                            .orElseGet(() -> {
                                log.debug("User not found by email, trying phone lookup");
                                return userRepository.findByPhone(username)
                                        .orElseThrow(() -> {
                                            log.warn("User not found with identifier: {}", username);
                                            return new UsernameNotFoundException("User Not Found: " + username);
                                        });
                            });
                });

        log.debug("User found: {} with role: {}", user.getId(), user.getRole());

        if (StringUtils.hasText(user.getOauth2Provider()) &&
                (user.getPasswordHash() == null || user.getPasswordHash().isEmpty())) {
            log.warn("OAuth2 user {} attempted password login - provider: {}",
                    user.getId(), user.getOauth2Provider());
            throw new UsernameNotFoundException("User registered with " + user.getOauth2Provider() +
                    ". Please use " + user.getOauth2Provider() + " login.");
        }

        log.info("Successfully loaded user: {} via username lookup", user.getId());
        return CustomUserDetails.build(user);
    }

    /**
     * Loads user details by user ID for JWT token validation.
     *
     * <p>This method is primarily used during JWT token validation to retrieve
     * the user associated with a specific user ID stored in the token.</p>
     *
     * @param userId the ID of the user to load
     * @return the fully populated UserDetails object for the user
     * @throws UsernameNotFoundException when no user is found with the given ID
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.info("Attempting to load user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User Not Found with id: " + userId);
                });

        log.info("Successfully loaded user by ID: {} with role: {}", userId, user.getRole());
        return CustomUserDetails.build(user);
    }

    /**
     * Attempts to parse the username as a UUID and find the user by ID.
     *
     * <p>This helper method provides a fallback mechanism to support JWT token
     * validation where the subject might be stored as a numeric ID string.</p>
     *
     * @param username the string to parse as a numeric ID
     * @return an Optional containing the User if found, or empty if the string
     *         is not a valid numeric ID or no user exists with that ID
     */
    private Optional<User> tryLoadById(String username) {
        try {
            Long userId = Long.valueOf(username);
            log.debug("Username parsed as numeric ID: {}", userId);
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                log.debug("User found by ID: {}", userId);
            }
            return user;
        } catch (IllegalArgumentException e) {
            log.debug("Username '{}' is not a valid numeric ID format", username);
            return Optional.empty();
        }
    }
}
