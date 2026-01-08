package com.ffms.resqeats.user.service;

import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.merchant.entity.Merchant;
import com.ffms.resqeats.merchant.repository.MerchantRepository;
import com.ffms.resqeats.outlet.entity.Outlet;
import com.ffms.resqeats.outlet.repository.OutletRepository;
import com.ffms.resqeats.user.dto.UpdateUserRequest;
import com.ffms.resqeats.user.dto.UserDto;
import com.ffms.resqeats.user.dto.UserFilterDto;
import com.ffms.resqeats.user.dto.UserListResponseDto;
import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import com.ffms.resqeats.user.repository.UserRepository;
import com.ffms.resqeats.user.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for user profile and account management operations.
 *
 * <p>Provides functionality for:</p>
 * <ul>
 *   <li>User profile retrieval and updates</li>
 *   <li>Password management</li>
 *   <li>Account status management (deactivation, suspension)</li>
 *   <li>Admin user management operations</li>
 *   <li>Outlet user creation for merchants</li>
 * </ul>
 *
 * @author ResqEats Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final OutletRepository outletRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves the profile of a user by their ID.
     *
     * @param userId the user ID to retrieve
     * @return the user profile DTO
     * @throws BusinessException if the user is not found
     */
    public UserDto getUserProfile(UUID userId) {
        log.info("Retrieving user profile for userId: {}", userId);
        User user = getUserOrThrow(userId);
        log.debug("User profile retrieved successfully for userId: {}", userId);
        return toDto(user);
    }

    /**
     * Updates a user's profile information.
     *
     * @param userId the user ID to update
     * @param request the update request containing new profile data
     * @return the updated user profile DTO
     * @throws BusinessException if the user is not found
     */
    @Transactional
    public UserDto updateProfile(UUID userId, UpdateUserRequest request) {
        log.info("Updating user profile for userId: {}", userId);
        User user = getUserOrThrow(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        user = userRepository.save(user);
        log.info("User profile updated successfully for userId: {}", userId);
        return toDto(user);
    }

    /**
     * Changes a user's password.
     *
     * @param userId the user ID
     * @param currentPassword the current password for verification
     * @param newPassword the new password to set
     * @throws BusinessException if the user is not found or current password is incorrect
     */
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        log.info("Processing password change request for userId: {}", userId);
        User user = getUserOrThrow(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            log.warn("Password change failed - incorrect current password for userId: {}", userId);
            throw new BusinessException("AUTH_005", "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for userId: {}", userId);
    }

    /**
     * Deactivates a user account.
     *
     * @param userId the user ID to deactivate
     * @throws BusinessException if the user is not found
     */
    @Transactional
    public void deactivateAccount(UUID userId) {
        log.info("Processing account deactivation for userId: {}", userId);
        User user = getUserOrThrow(userId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("Account deactivated successfully for userId: {}", userId);
    }

    /**
     * Retrieves all users with pagination and filtering (admin operation).
     *
     * @param filter the filter criteria
     * @param pageable the pagination parameters
     * @return a page of user list response DTOs
     */
    public Page<UserListResponseDto> getAllUsers(UserFilterDto filter, Pageable pageable) {
        log.info("Retrieving all users with filter: {}, page={}, size={}", filter, pageable.getPageNumber(), pageable.getPageSize());
        Page<UserListResponseDto> result = userRepository.findAll(UserSpecification.filterBy(filter), pageable).map(this::toListDto);
        log.debug("Retrieved {} users", result.getTotalElements());
        return result;
    }

    /**
     * Searches users by query string (admin operation).
     *
     * @param query the search query (matches email, first name, or last name)
     * @param pageable the pagination parameters
     * @return a page of matching user list response DTOs
     */
    public Page<UserListResponseDto> searchUsers(String query, Pageable pageable) {
        log.info("Searching users with query: '{}', page={}, size={}", query, pageable.getPageNumber(), pageable.getPageSize());
        Page<UserListResponseDto> result = userRepository.findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query, query, query, pageable).map(this::toListDto);
        log.debug("Search returned {} users matching query: '{}'", result.getTotalElements(), query);
        return result;
    }

    /**
     * Retrieves a user by ID (admin operation).
     *
     * @param userId the user ID to retrieve
     * @return the user profile DTO
     * @throws BusinessException if the user is not found
     */
    public UserDto getUserById(UUID userId) {
        log.info("Admin retrieving user by userId: {}", userId);
        return toDto(getUserOrThrow(userId));
    }

    /**
     * Suspends a user account (admin operation).
     *
     * @param userId the user ID to suspend
     * @param reason the reason for suspension
     * @return the updated user profile DTO
     * @throws BusinessException if the user is not found
     */
    @Transactional
    public UserDto suspendUser(UUID userId, String reason) {
        log.info("Suspending user account for userId: {}, reason: {}", userId, reason);
        User user = getUserOrThrow(userId);
        user.setStatus(UserStatus.SUSPENDED);
        user = userRepository.save(user);
        log.warn("User account suspended - userId: {}, reason: {}", userId, reason);
        return toDto(user);
    }

    /**
     * Reactivates a suspended user account (admin operation).
     *
     * @param userId the user ID to reactivate
     * @return the updated user profile DTO
     * @throws BusinessException if the user is not found
     */
    @Transactional
    public UserDto reactivateUser(UUID userId) {
        log.info("Reactivating user account for userId: {}", userId);
        User user = getUserOrThrow(userId);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);
        log.info("User account reactivated successfully for userId: {}", userId);
        return toDto(user);
    }

    /**
     * Creates an outlet user account for a merchant.
     *
     * @param outletId the outlet ID to associate the user with
     * @param email the user's email address
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param merchantId the merchant ID the user belongs to
     * @return the created user profile DTO
     * @throws BusinessException if the email already exists
     */
    @Transactional
    public UserDto createOutletUser(UUID outletId, String email, String firstName, String lastName, UUID merchantId) {
        log.info("Creating outlet user - email: {}, outletId: {}, merchantId: {}", email, outletId, merchantId);
        
        if (userRepository.existsByEmail(email)) {
            log.warn("Outlet user creation failed - email already exists: {}", email);
            throw new BusinessException("USER_001", "Email already exists");
        }

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(UserRole.OUTLET_USER)
                .outletId(outletId)
                .merchantId(merchantId)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPasswordHash(passwordEncoder.encode(tempPassword));

        user = userRepository.save(user);
        log.info("Outlet user created successfully - userId: {}, outletId: {}", user.getId(), outletId);

        return toDto(user);
    }

    /**
     * Retrieves a user by ID or throws an exception if not found.
     *
     * @param userId the user ID to retrieve
     * @return the user entity
     * @throws BusinessException if the user is not found
     */
    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found - userId: {}", userId);
                    return new BusinessException("USER_002", "User not found");
                });
    }

    /**
     * Converts a User entity to a UserDto.
     *
     * @param user the user entity
     * @return the user DTO
     */
    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .merchantId(user.getMerchantId())
                .outletId(user.getOutletId())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Converts a User entity to a UserListResponseDto for list display.
     *
     * @param user the user entity
     * @return the user list response DTO with association data
     */
    private UserListResponseDto toListDto(User user) {
        UserListResponseDto.UserListResponseDtoBuilder builder = UserListResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .createdAt(user.getCreatedAt());

        // Add merchant association for MERCHANT role users
        if (user.getRole() == UserRole.MERCHANT && user.getMerchantId() != null) {
            merchantRepository.findById(user.getMerchantId()).ifPresent(merchant -> {
                builder.merchantAssociation(UserListResponseDto.MerchantAssociation.builder()
                        .merchantId(merchant.getId())
                        .merchantName(merchant.getName())
                        .merchantLogoUrl(merchant.getLogoUrl())
                        .merchantContactEmail(merchant.getContactEmail())
                        .merchantContactPhone(merchant.getContactPhone())
                        .build());
            });
        }

        // Add outlet association for OUTLET_USER role users
        if (user.getRole() == UserRole.OUTLET_USER && user.getOutletId() != null) {
            outletRepository.findById(user.getOutletId()).ifPresent(outlet -> {
                String merchantName = null;
                if (outlet.getMerchantId() != null) {
                    merchantName = merchantRepository.findById(outlet.getMerchantId())
                            .map(Merchant::getName)
                            .orElse(null);
                }
                builder.outletAssociation(UserListResponseDto.OutletAssociation.builder()
                        .outletId(outlet.getId())
                        .outletName(outlet.getName())
                        .outletAddress(outlet.getAddress())
                        .outletCity(outlet.getCity())
                        .merchantId(outlet.getMerchantId())
                        .merchantName(merchantName)
                        .build());
            });
        }

        return builder.build();
    }
}
