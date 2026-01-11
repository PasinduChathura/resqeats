package com.ffms.resqeats.user.service;

import com.ffms.resqeats.common.dto.MailDto;
import com.ffms.resqeats.common.exception.BusinessException;
import com.ffms.resqeats.merchant.repository.MerchantRepository;
import com.ffms.resqeats.outlet.repository.OutletRepository;
import com.ffms.resqeats.security.context.ResqeatsSecurityContext;
import com.ffms.resqeats.security.context.SecurityContextHolder;
import com.ffms.resqeats.user.dto.MerchantAssociation;
import com.ffms.resqeats.user.dto.OutletAssociation;
import com.ffms.resqeats.user.dto.RoleDto;
import com.ffms.resqeats.user.dto.UpdateUserRequest;
import com.ffms.resqeats.user.dto.UserDto;
import com.ffms.resqeats.user.dto.UserFilterDto;
import com.ffms.resqeats.user.dto.admin.AdminCreateUserRequest;
import com.ffms.resqeats.user.dto.admin.AdminUpdateUserRequest;
import com.ffms.resqeats.user.dto.admin.UserAdminDto;
import com.ffms.resqeats.user.dto.admin.UserAdminListDto;
import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserRole;
import com.ffms.resqeats.user.enums.UserStatus;
import com.ffms.resqeats.user.repository.UserRepository;
import com.ffms.resqeats.user.specification.UserSpecification;
import com.ffms.resqeats.util.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for user profile and account management operations.
 *
 * <p>Provides functionality for:</p>
 * <ul>
 *   <li>Current user profile retrieval and updates</li>
 *   <li>Password management</li>
 *   <li>Account status management (deactivation, suspension, reactivation)</li>
 *   <li>Admin user management operations (CRUD for all user types)</li>
 *   <li>Role-based user creation with proper validation</li>
 * </ul>
 *
 * <p>Security Context: Uses SecurityContextHolder for user identity and authorization.
 * No need to pass userId through method parameters for current user operations.</p>
 *
 * @author ResqEats Team
 * @version 2.0
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
    private final EmailSender emailSender;

    @Value("${app.mail.from:noreply@resqeats.com}")
    private String mailFrom;

    @Value("${app.name:ResqEats}")
    private String appName;

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private static final int GENERATED_PASSWORD_LENGTH = 12;

    // =====================
    // Current User Operations
    // =====================

    /**
     * Retrieves the profile of the currently authenticated user.
     */
    public UserDto getMyProfile() {
        Long userId = requireAuthenticatedUserId();
        log.debug("Retrieving profile for current user: {}", userId);
        return toDto(getUserOrThrow(userId));
    }

    /**
     * Updates the profile of the currently authenticated user.
     */
    @Transactional
    public UserDto updateMyProfile(UpdateUserRequest request) {
        Long userId = requireAuthenticatedUserId();
        log.info("Updating profile for current user: {}", userId);
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
        log.info("Profile updated for user: {}", userId);
        return toDto(user);
    }

    /**
     * Changes the password of the currently authenticated user.
     */
    @Transactional
    public void changeMyPassword(String currentPassword, String newPassword) {
        Long userId = requireAuthenticatedUserId();
        log.info("Processing password change for user: {}", userId);
        User user = getUserOrThrow(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            log.warn("Password change failed - incorrect current password for user: {}", userId);
            throw new BusinessException("AUTH_005", "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    /**
     * Deactivates the currently authenticated user's account.
     */
    @Transactional
    public void deactivateMyAccount() {
        Long userId = requireAuthenticatedUserId();
        log.info("Deactivating account for user: {}", userId);
        User user = getUserOrThrow(userId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("Account deactivated for user: {}", userId);
    }

    // =====================
    // Admin User Management
    // =====================

    /**
     * Creates a new user account (Admin operation).
     * <p>
     * Role creation rules:
     * - SUPER_ADMIN can create any role
     * - ADMIN can create MERCHANT_USER, OUTLET_USER, CUSTOMER_USER
     * <p>
     * Association requirements:
     * - MERCHANT_USER: merchantId required
     * - OUTLET_USER: outletId required
     * - ADMIN/CUSTOMER_USER: no association required
     * <p>
     * Password handling:
     * - If password provided: use it
     * - If password null: generate temporary password and email it
     *
     * @param request the user creation request
     * @return the created user DTO
     */
    @Transactional
    public UserAdminDto createUser(AdminCreateUserRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Creating user - email: {}, role: {}, by admin: {}",
                request.getEmail(), request.getRole(), context.getUserId());

        // Validate role creation permissions
        validateRoleCreationPermission(context, request.getRole());

        // Validate contact info - at least email or phone required
        if ((request.getEmail() == null || request.getEmail().isBlank()) &&
                (request.getPhone() == null || request.getPhone().isBlank())) {
            throw new BusinessException("USER_001", "Either email or phone is required");
        }

        // Validate email uniqueness
        if (request.getEmail() != null && !request.getEmail().isBlank() &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("USER_001", "Email already exists");
        }

        // Validate phone uniqueness
        if (request.getPhone() != null && !request.getPhone().isBlank() &&
                userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("USER_001", "Phone number already exists");
        }

        // Validate role-specific requirements
        validateRoleAssociations(request.getRole(), request.getMerchantId(), request.getOutletId());

        // Handle password
        String rawPassword = request.getPassword();
        boolean passwordGenerated = false;
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = generateSecurePassword();
            passwordGenerated = true;
        }

        // Build and save user
        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .profileImageUrl(request.getProfileImageUrl())
                .role(request.getRole())
                .merchantId(getMerchantIdForRole(request.getRole(), request.getMerchantId()))
                .outletId(getOutletIdForRole(request.getRole(), request.getOutletId()))
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User created - id: {}, role: {}", user.getId(), user.getRole());

        // Send welcome email if password was generated and email exists
        if (passwordGenerated && request.getEmail() != null && !request.getEmail().isBlank()) {
            if (Boolean.TRUE.equals(request.getSendWelcomeEmail())) {
                sendWelcomeEmail(user, rawPassword);
            }
        }

        return toAdminDto(userRepository.findById(user.getId()).orElse(user));
    }

    /**
     * Updates a user (Admin operation).
     * Allows updating any user field with proper validation.
     *
     * @param userId  the user ID to update
     * @param request the update request
     * @return the updated user DTO
     */
    @Transactional
    public UserAdminDto updateUser(Long userId, AdminUpdateUserRequest request) {
        var context = SecurityContextHolder.getContext();
        log.info("Admin updating user: {} by admin: {}", userId, context.getUserId());

        User user = getUserOrThrow(userId);

        UserRole effectiveRole = request.getRole() != null ? request.getRole() : user.getRole();

        Long requestedOrExistingMerchantId = request.getMerchantId() != null ? request.getMerchantId() : user.getMerchantId();
        Long requestedOrExistingOutletId = request.getOutletId() != null ? request.getOutletId() : user.getOutletId();

        Long normalizedMerchantId = getMerchantIdForRole(effectiveRole, requestedOrExistingMerchantId);
        Long normalizedOutletId = getOutletIdForRole(effectiveRole, requestedOrExistingOutletId);

        // Validate role change permissions
        if (request.getRole() != null && request.getRole() != user.getRole()) {
            validateRoleCreationPermission(context, request.getRole());
        }
    
        validateRoleAssociations(effectiveRole, normalizedMerchantId, normalizedOutletId);

        // Update basic fields
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("USER_001", "Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            if (!request.getPhone().equals(user.getPhone()) &&
                    userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("USER_001", "Phone number already exists");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        user.setMerchantId(normalizedMerchantId);
        user.setOutletId(normalizedOutletId);
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getPhoneVerified() != null) {
            user.setPhoneVerified(request.getPhoneVerified());
        }

        // Handle password update
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            log.info("Password updated for user: {}", userId);
        } else if (Boolean.TRUE.equals(request.getSendPasswordReset())) {
            String newPassword = generateSecurePassword();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                sendPasswordResetEmail(user, newPassword);
            }
        }

        user = userRepository.save(user);
        log.info("User updated: {}", userId);
        return toAdminDto(userRepository.findById(user.getId()).orElse(user));
    }

    /**
     * Gets a user by ID (Admin operation).
     */
    public UserAdminDto getAdminUserById(Long userId) {
        log.debug("Admin retrieving user: {}", userId);
        return toAdminDto(getUserOrThrow(userId));
    }

    /**
     * Gets all users with filtering and pagination (Admin operation).
     */
    public Page<UserAdminListDto> getAllUsersAdmin(UserFilterDto filter, Pageable pageable) {
        log.debug("Admin listing users - filter: {}, page: {}", filter, pageable.getPageNumber());
        return userRepository.findAll(UserSpecification.filterBy(filter), pageable)
                .map(this::toAdminListDto);
    }

    /**
     * Suspends a user account (Admin operation).
     */
    @Transactional
    public UserAdminDto suspendUser(Long userId, String reason) {
        var context = SecurityContextHolder.getContext();
        log.info("Suspending user: {} by admin: {}, reason: {}", userId, context.getUserId(), reason);

        User user = getUserOrThrow(userId);

        // Cannot suspend yourself
        if (userId.equals(context.getUserId())) {
            throw new BusinessException("USER_004", "Cannot suspend your own account");
        }

        // Cannot suspend higher-privileged users
        if (user.getRole() != null && context.getRole() != null &&
                user.getRole().getHierarchyLevel() <= context.getRole().getHierarchyLevel()) {
            throw new BusinessException("USER_004", "Cannot suspend user with equal or higher privileges");
        }

        user.setStatus(UserStatus.SUSPENDED);
        user = userRepository.save(user);
        log.warn("User suspended: {} - reason: {}", userId, reason);
        return toAdminDto(user);
    }

    /**
     * Reactivates a suspended user account (Admin operation).
     */
    @Transactional
    public UserAdminDto reactivateUser(Long userId) {
        var context = SecurityContextHolder.getContext();
        log.info("Reactivating user: {} by admin: {}", userId, context.getUserId());

        User user = getUserOrThrow(userId);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);
        log.info("User reactivated: {}", userId);
        return toAdminDto(user);
    }

    /**
     * Deletes a user (soft delete by setting status to INACTIVE).
     */
    @Transactional
    public void deleteUser(Long userId) {
        var context = SecurityContextHolder.getContext();
        log.info("Deleting user: {} by admin: {}", userId, context.getUserId());

        User user = getUserOrThrow(userId);

        // Cannot delete yourself
        if (userId.equals(context.getUserId())) {
            throw new BusinessException("USER_004", "Cannot delete your own account");
        }

        // Cannot delete higher-privileged users
        if (user.getRole() != null && context.getRole() != null &&
                user.getRole().getHierarchyLevel() <= context.getRole().getHierarchyLevel()) {
            throw new BusinessException("USER_004", "Cannot delete user with equal or higher privileges");
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.warn("User deleted (soft): {}", userId);
    }

    /**
     * Resets a user's password and sends email notification.
     */
    @Transactional
    public void resetUserPassword(Long userId) {
        var context = SecurityContextHolder.getContext();
        log.info("Resetting password for user: {} by admin: {}", userId, context.getUserId());

        User user = getUserOrThrow(userId);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException("USER_005", "User has no email address for password reset");
        }

        String newPassword = generateSecurePassword();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        sendPasswordResetEmail(user, newPassword);
        log.info("Password reset for user: {}", userId);
    }

    // =====================
    // Legacy Support (for backward compatibility)
    // =====================

    /**
     * Retrieves the profile of a user by their ID.
     */
    public UserDto getUserProfile(Long userId) {
        log.debug("Retrieving user profile for userId: {}", userId);
        return toDto(getUserOrThrow(userId));
    }

    /**
     * Updates a user's profile information.
     */
    @Transactional
    public UserDto updateProfile(Long userId, UpdateUserRequest request) {
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
        log.info("User profile updated for userId: {}", userId);
        return toDto(user);
    }

    /**
     * Changes a user's password.
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Processing password change for userId: {}", userId);
        User user = getUserOrThrow(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            log.warn("Password change failed - incorrect current password for userId: {}", userId);
            throw new BusinessException("AUTH_005", "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for userId: {}", userId);
    }

    /**
     * Deactivates a user account.
     */
    @Transactional
    public void deactivateAccount(Long userId) {
        log.info("Deactivating account for userId: {}", userId);
        User user = getUserOrThrow(userId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("Account deactivated for userId: {}", userId);
    }

    /**
     * Retrieves all users with pagination and filtering.
     */
    public Page<UserDto> getAllUsers(UserFilterDto filter, Pageable pageable) {
        log.debug("Retrieving all users - filter: {}, page: {}", filter, pageable.getPageNumber());
        return userRepository.findAll(UserSpecification.filterBy(filter), pageable).map(this::toDto);
    }

    /**
     * Retrieves a user by ID.
     */
    public UserDto getUserById(Long userId) {
        log.debug("Retrieving user by userId: {}", userId);
        return toDto(getUserOrThrow(userId));
    }

    /**
     * Returns all available roles with their hierarchy level and description.
     */
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        var context = SecurityContextHolder.getContext();
        log.debug("Retrieving all user roles");

        List<RoleDto> roles = new ArrayList<>();
        for (UserRole role : UserRole.values()) {
            // Only show roles that the current user can create
            if (context != null && !context.isAnonymous() && !canCreateRole(context, role)) {
                continue;
            }

            String desc;
            String display;
            switch (role) {
                case SUPER_ADMIN:
                    desc = "Full platform access, global system configuration, security control, and role management";
                    display = "Super Admin";
                    break;
                case ADMIN:
                    desc = "Full platform access, user management, system settings";
                    display = "Admin";
                    break;
                case MERCHANT_USER:
                    desc = "Manage outlets, items, view analytics for owned merchants";
                    display = "Merchant User";
                    break;
                case OUTLET_USER:
                    desc = "Manage orders, inventory at assigned outlet location";
                    display = "Outlet User";
                    break;
                case CUSTOMER_USER:
                default:
                    desc = "Browse products, place orders, manage profile, and track purchases";
                    display = "Customer";
                    break;
            }
            roles.add(RoleDto.builder()
                    .name(role)
                    .hierarchyLevel(role.getHierarchyLevel())
                    .displayName(display)
                    .description(desc)
                    .build());
        }
        return roles;
    }

    // =====================
    // Validation Helpers
    // =====================

    private void validateRoleCreationPermission(ResqeatsSecurityContext context, UserRole targetRole) {
        if (!canCreateRole(context, targetRole)) {
            log.warn("Unauthorized role creation attempt - admin: {}, target role: {}",
                    context.getUserId(), targetRole);
            throw new BusinessException("USER_003", "Insufficient permissions to create user with role: " + targetRole);
        }
    }

    private boolean canCreateRole(ResqeatsSecurityContext context, UserRole targetRole) {
        if (context.isSuperAdmin()) {
            return true; // SUPER_ADMIN can create any role
        }
        if (context.isAdmin()) {
            // ADMIN can create MERCHANT_USER, OUTLET_USER, CUSTOMER_USER
            return  targetRole == UserRole.ADMIN ||
                    targetRole == UserRole.MERCHANT_USER ||
                    targetRole == UserRole.OUTLET_USER ||
                    targetRole == UserRole.CUSTOMER_USER;
        }
        return false;
    }

    private void validateRoleAssociations(UserRole role, Long merchantId, Long outletId) {
        switch (role) {
            case MERCHANT_USER:
                if (merchantId == null) {
                    throw new BusinessException("USER_003", "Merchant ID is required for MERCHANT_USER role");
                }
                if (outletId != null) {
                    throw new BusinessException("USER_003", "Outlet ID is prohibited for MERCHANT_USER role");
                }
                if (!merchantRepository.existsById(merchantId)) {
                    throw new BusinessException("USER_003", "Merchant not found: " + merchantId);
                }
                break;
            case OUTLET_USER:
                if (outletId == null) {
                    throw new BusinessException("USER_003", "Outlet ID is required for OUTLET_USER role");
                }
                if (merchantId != null) {
                    throw new BusinessException("USER_003", "Merchant ID is prohibited for OUTLET_USER role");
                }
                if (!outletRepository.existsById(outletId)) {
                    throw new BusinessException("USER_003", "Outlet not found: " + outletId);
                }
                break;
            case SUPER_ADMIN:
            case ADMIN:
            case CUSTOMER_USER:
                // No associations required
                break;
        }
    }

    private Long getMerchantIdForRole(UserRole role, Long merchantId) {
        if (role == UserRole.MERCHANT_USER) {
            return merchantId;
        }
        return null;
    }

    private Long getOutletIdForRole(UserRole role, Long outletId) {
        if (role == UserRole.OUTLET_USER) {
            return outletId;
        }
        return null;
    }

    // =====================
    // Password Generation
    // =====================

    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    // =====================
    // Email Notifications
    // =====================

    private void sendWelcomeEmail(User user, String password) {
        try {
            String subject = String.format("Welcome to %s - Your Account Details", appName);
            String body = buildWelcomeEmailBody(user, password);

            MailDto mail = new MailDto();
            mail.setFrom(mailFrom);
            mail.setTo(new String[]{user.getEmail()});
            mail.setCc(new String[]{});
            mail.setBcc(new String[]{});
            mail.setSubject(subject);
            mail.setBody(body);

            emailSender.sendEmail(mail);
            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
            // Don't throw - user creation should still succeed
        }
    }

    private void sendPasswordResetEmail(User user, String newPassword) {
        try {
            String subject = String.format("%s - Your Password Has Been Reset", appName);
            String body = buildPasswordResetEmailBody(user, newPassword);

            MailDto mail = new MailDto();
            mail.setFrom(mailFrom);
            mail.setTo(new String[]{user.getEmail()});
            mail.setCc(new String[]{});
            mail.setBcc(new String[]{});
            mail.setSubject(subject);
            mail.setBody(body);

            emailSender.sendEmail(mail);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    private String buildWelcomeEmailBody(User user, String password) {
        String name = user.getFirstName() != null ? user.getFirstName() : "User";
        return String.format("""
                Hello %s,
                
                Welcome to %s! Your account has been created successfully.
                
                Your login credentials:
                Email: %s
                Temporary Password: %s
                
                Please log in and change your password immediately for security purposes.
                
                If you did not request this account, please contact our support team.
                
                Best regards,
                The %s Team
                """, name, appName, user.getEmail(), password, appName);
    }

    private String buildPasswordResetEmailBody(User user, String newPassword) {
        String name = user.getFirstName() != null ? user.getFirstName() : "User";
        return String.format("""
                Hello %s,
                
                Your password has been reset by an administrator.
                
                Your new temporary password: %s
                
                Please log in and change your password immediately for security purposes.
                
                If you did not request this reset, please contact our support team immediately.
                
                Best regards,
                The %s Team
                """, name, newPassword, appName);
    }

    // =====================
    // Internal Helpers
    // =====================

    /**
     * Retrieves a user by ID or throws an exception if not found.
     */
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new BusinessException("USER_002", "User not found");
                });
    }

    private Long requireAuthenticatedUserId() {
        var context = SecurityContextHolder.getContext();
        if (context == null || context.isAnonymous() || context.getUserId() == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return context.getUserId();
    }

    // =====================
    // DTO Mappers
    // =====================

    private UserDto toDto(User user) {
        UserDto.UserDtoBuilder builder = UserDto.builder()
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

        // MERCHANT_USER role association
        if (user.getRole() == UserRole.MERCHANT_USER && user.getMerchantId() != null) {
            var merchant = user.getMerchant();
            builder.merchantAssociation(MerchantAssociation.builder()
                    .merchantId(user.getMerchantId())
                    .merchantName(merchant != null ? merchant.getName() : null)
                    .merchantLogoUrl(merchant != null ? merchant.getLogoUrl() : null)
                    .merchantContactEmail(merchant != null ? merchant.getContactEmail() : null)
                    .merchantContactPhone(merchant != null ? merchant.getContactPhone() : null)
                    .build());
        }

        // OUTLET_USER role association
        if (user.getRole() == UserRole.OUTLET_USER && user.getOutletId() != null) {
            var outlet = user.getOutlet();
            Long outletMerchantId = outlet != null ? outlet.getMerchantId() : null;
            String outletMerchantName = null;
            if (outletMerchantId != null) {
            outletMerchantName = merchantRepository.findById(outletMerchantId)
                .map(m -> m.getName())
                .orElse(null);
            }
            builder.outletAssociation(OutletAssociation.builder()
                    .outletId(user.getOutletId())
                    .outletName(outlet != null ? outlet.getName() : null)
                    .outletAddress(outlet != null ? outlet.getAddress() : null)
                    .outletCity(outlet != null ? outlet.getCity() : null)
                .merchantId(outletMerchantId)
                .merchantName(outletMerchantName)
                    .build());
        }

        return builder.build();
    }

    private UserAdminDto toAdminDto(User user) {
        Long resolvedMerchantId = user.getMerchantId() != null
            ? user.getMerchantId()
            : (user.getOutlet() != null ? user.getOutlet().getMerchantId() : null);

        String resolvedMerchantName = user.getMerchant() != null
            ? user.getMerchant().getName()
            : (resolvedMerchantId != null
            ? merchantRepository.findById(resolvedMerchantId).map(m -> m.getName()).orElse(null)
            : null);

        return UserAdminDto.builder()
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
            .merchantId(resolvedMerchantId)
            .merchantName(resolvedMerchantName)
                .outletId(user.getOutletId())
                .outletName(user.getOutlet() != null ? user.getOutlet().getName() : null)
                .oauth2Provider(user.getOauth2Provider())
                .pushNotificationsEnabled(user.getPushNotificationsEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    private UserAdminListDto toAdminListDto(User user) {
        String merchantName = user.getMerchant() != null
            ? user.getMerchant().getName()
            : (user.getOutlet() != null && user.getOutlet().getMerchantId() != null
            ? merchantRepository.findById(user.getOutlet().getMerchantId()).map(m -> m.getName()).orElse(null)
            : null);

        return UserAdminListDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .status(user.getStatus())
            .merchantName(merchantName)
                .outletName(user.getOutlet() != null ? user.getOutlet().getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
