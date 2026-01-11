package com.ffms.resqeats.user.controller;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.dto.PageResponse;
import com.ffms.resqeats.user.dto.RoleDto;
import com.ffms.resqeats.user.dto.UpdateUserRequest;
import com.ffms.resqeats.user.dto.UserDto;
import com.ffms.resqeats.user.dto.UserFilterDto;
import com.ffms.resqeats.user.dto.admin.AdminCreateUserRequest;
import com.ffms.resqeats.user.dto.admin.AdminUpdateUserRequest;
import com.ffms.resqeats.user.dto.admin.UserAdminDto;
import com.ffms.resqeats.user.dto.admin.UserAdminListDto;
import com.ffms.resqeats.user.enums.UserStatus;
import com.ffms.resqeats.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User controller per SRS Section 6.2.
 * 
 * All endpoints use unified paths - scope filtering is applied automatically
 * at the repository level based on the authenticated user's role and context.
 *
 * User Endpoints:
 * GET /users/me - Get current user profile
 * PUT /users/me - Update profile
 * PUT /users/password - Change password
 * DELETE /users/me - Deactivate account
 *
 * Admin Endpoints:
 * POST /users - Create user (ADMIN - supports all roles)
 * GET /users - List users with filters (scoped by role)
 * GET /users/{id} - Get user details (ADMIN)
 * PUT /users/{id} - Update user (ADMIN)
 * DELETE /users/{id} - Delete user (ADMIN - soft delete)
 * POST /users/{id}/suspend - Suspend user (ADMIN)
 * POST /users/{id}/reactivate - Reactivate user (ADMIN)
 * POST /users/{id}/reset-password - Reset user password (ADMIN)
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    // =====================
    // User Endpoints
    // =====================

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        UserDto user = userService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto user = userService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success(user, "Profile updated"));
    }

    @PutMapping("/password")
    @Operation(summary = "Change password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changeMyPassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed"));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Deactivate account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount() {
        userService.deactivateMyAccount();
        return ResponseEntity.ok(ApiResponse.success(null, "Account deactivated"));
    }

    // =====================
    // Admin Endpoints
    // =====================

    @GetMapping
    @Operation(summary = "List users with filters (scoped by role)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserAdminListDto>>> getUsers(
            UserFilterDto filter,
            Pageable pageable) {
        Page<UserAdminListDto> users = userService.getAllUsersAdmin(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(users)));
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all supported roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        List<RoleDto> roles = userService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get all supported user statuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserStatus>>> getAllUserStatuses() {
        return ResponseEntity.ok(ApiResponse.success(List.of(UserStatus.values())));
    }

    // =====================
    // User Action Endpoints (Admin)
    // =====================

    @GetMapping("/{id}")
    @Operation(summary = "Get user details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminDto>> getUserById(@PathVariable Long id) {
        UserAdminDto user = userService.getAdminUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @Operation(summary = "Create user (Admin - supports ADMIN, MERCHANT_USER, OUTLET_USER, CUSTOMER_USER)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminDto>> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        UserAdminDto user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(user, "User created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        UserAdminDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user (soft delete)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminDto>> suspendUser(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendRequest request) {
        String reason = request != null ? request.getReason() : "Admin action";
        UserAdminDto user = userService.suspendUser(id, reason);
        return ResponseEntity.ok(ApiResponse.success(user, "User suspended"));
    }

    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAdminDto>> reactivateUser(@PathVariable Long id) {
        UserAdminDto user = userService.reactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User reactivated"));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset user password and send email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetUserPassword(@PathVariable Long id) {
        userService.resetUserPassword(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset email sent"));
    }

    // Request DTOs

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.ToString(exclude = {"currentPassword", "newPassword"})
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;

        @NotBlank
        private String newPassword;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuspendRequest {
        private String reason;
    }
}
