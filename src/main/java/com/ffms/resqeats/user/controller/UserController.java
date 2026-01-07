package com.ffms.resqeats.user.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.user.dto.UpdateUserRequest;
import com.ffms.resqeats.user.dto.UserDto;
import com.ffms.resqeats.user.service.UserService;
import com.ffms.resqeats.security.CurrentUser;
import com.ffms.resqeats.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User controller per SRS Section 6.2.
 *
 * User Endpoints:
 * GET /users/me - Get current user profile
 * PUT /users/me - Update profile
 * PUT /users/me/password - Change password
 * DELETE /users/me - Deactivate account
 *
 * Admin Endpoints:
 * GET /admin/users - List all users
 * GET /admin/users/{id} - Get user details
 * POST /admin/users/{id}/suspend - Suspend user
 * POST /admin/users/{id}/reactivate - Reactivate user
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    // =====================
    // User Endpoints
    // =====================

    @GetMapping("/users/me")
    @Operation(summary = "Get current user profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        log.info("Get current user profile request for userId: {}", currentUser.getId());
        try {
            UserDto user = userService.getUserProfile(currentUser.getId());
            log.info("Successfully retrieved profile for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            log.error("Failed to get profile for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/users/me")
    @Operation(summary = "Update profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Update profile request for userId: {}", currentUser.getId());
        try {
            UserDto user = userService.updateProfile(currentUser.getId(), request);
            log.info("Profile updated successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(user, "Profile updated"));
        } catch (Exception e) {
            log.error("Failed to update profile for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/users/me/password")
    @Operation(summary = "Change password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request for userId: {}", currentUser.getId());
        try {
            userService.changePassword(currentUser.getId(), request.getCurrentPassword(), request.getNewPassword());
            log.info("Password changed successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Password changed"));
        } catch (Exception e) {
            log.warn("Password change failed for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/users/me")
    @Operation(summary = "Deactivate account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(@CurrentUser UserPrincipal currentUser) {
        log.info("Deactivate account request for userId: {}", currentUser.getId());
        try {
            userService.deactivateAccount(currentUser.getId());
            log.info("Account deactivated successfully for userId: {}", currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Account deactivated"));
        } catch (Exception e) {
            log.error("Failed to deactivate account for userId: {} - Error: {}", currentUser.getId(), e.getMessage());
            throw e;
        }
    }

    // =====================
    // Admin Endpoints
    // =====================

    @GetMapping("/admin/users")
    @Operation(summary = "List all users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getAllUsers(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        log.info("Admin: List all users request - query: {}, page: {}", query, pageable.getPageNumber());
        try {
            Page<UserDto> users = query != null
                    ? userService.searchUsers(query, pageable)
                    : userService.getAllUsers(pageable);
            log.info("Admin: Retrieved {} users", users.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(users)));
        } catch (Exception e) {
            log.error("Admin: Failed to list users - Error: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/admin/users/{id}")
    @Operation(summary = "Get user details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        log.info("Admin: Get user details request for userId: {}", id);
        try {
            UserDto user = userService.getUserById(id);
            log.info("Admin: Successfully retrieved user: {}", id);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            log.error("Admin: Failed to get user: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/admin/users/{id}/suspend")
    @Operation(summary = "Suspend user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> suspendUser(
            @PathVariable UUID id,
            @RequestBody(required = false) SuspendRequest request) {
        String reason = request != null ? request.getReason() : "Admin action";
        log.info("Admin: Suspend user request for userId: {} - Reason: {}", id, reason);
        try {
            UserDto user = userService.suspendUser(id, reason);
            log.info("Admin: User suspended successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(user, "User suspended"));
        } catch (Exception e) {
            log.error("Admin: Failed to suspend user: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/admin/users/{id}/reactivate")
    @Operation(summary = "Reactivate user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> reactivateUser(@PathVariable UUID id) {
        log.info("Admin: Reactivate user request for userId: {}", id);
        try {
            UserDto user = userService.reactivateUser(id);
            log.info("Admin: User reactivated successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(user, "User reactivated"));
        } catch (Exception e) {
            log.error("Admin: Failed to reactivate user: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // Request DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.ToString(exclude = {"currentPassword", "newPassword"})
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuspendRequest {
        private String reason;
    }
}
