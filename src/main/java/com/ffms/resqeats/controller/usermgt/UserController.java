package com.ffms.resqeats.controller.usermgt;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.model.Status;
import com.ffms.resqeats.dto.usermgt.password.PasswordDto;
import com.ffms.resqeats.dto.usermgt.user.UserDto;
import com.ffms.resqeats.dto.usermgt.user.UserRegisterDto;
import com.ffms.resqeats.dto.usermgt.user.UserUpdateDto;
import com.ffms.resqeats.enums.usermgt.UserType;
import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.service.usermgt.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management controller for CRUD operations on users.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    /**
     * Get all users (SUPER_ADMIN only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() throws Exception {
        List<UserDto> users = userService.findAllUsers().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get 'USER' type users
     */
    @GetMapping
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUserTypeUsers() throws Exception {
        List<UserDto> users = userService.findUsersByType(UserType.USER).stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get all 'ACTIVE' users
     */
    @GetMapping("/active")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllActiveUsers() throws Exception {
        List<UserDto> users = userService.findUsersByStatusAndType(Status.ACTIVE, UserType.USER).stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get all 'INACTIVE' users
     */
    @GetMapping("/inactive")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllInactiveUsers() throws Exception {
        List<UserDto> users = userService.findUsersByStatusAndType(Status.INACTIVE, UserType.USER).stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get all 'TERMINATED' users
     */
    @GetMapping("/terminated")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllTerminatedUsers() throws Exception {
        List<UserDto> users = userService.findUsersByStatusAndType(Status.TERMINATED, UserType.USER).stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long userId) throws Exception {
        UserDto user = modelMapper.map(userService.findByUserId(userId), UserDto.class);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Create a new user
     */
    @PostMapping
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.writePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody UserRegisterDto userDto) throws Exception {
        User user = userService.createUser(modelMapper.map(userDto, User.class));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(modelMapper.map(user, UserDto.class), "User created successfully"));
    }

    /**
     * Update user
     */
    @PatchMapping("/{userId}")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @Valid @RequestBody UserUpdateDto userDto,
            @PathVariable Long userId) throws Exception {
        User user = userService.updateUser(modelMapper.map(userDto, User.class), userId);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(user, UserDto.class), "User updated successfully"));
    }

    /**
     * Activate user
     */
    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<UserDto>> activateUser(@PathVariable Long userId) throws Exception {
        User user = userService.changeUserStatus(Status.ACTIVE, userId);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(user, UserDto.class), "User activated successfully"));
    }

    /**
     * Deactivate user
     */
    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<UserDto>> deactivateUser(@PathVariable Long userId) throws Exception {
        User user = userService.changeUserStatus(Status.INACTIVE, userId);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(user, UserDto.class), "User deactivated successfully"));
    }

    /**
     * Terminate user
     */
    @PatchMapping("/{userId}/terminate")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<UserDto>> terminateUser(@PathVariable Long userId) throws Exception {
        User user = userService.changeUserStatus(Status.TERMINATED, userId);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(user, UserDto.class), "User terminated successfully"));
    }

    /**
     * Change user password
     */
    @PostMapping("/{userId}/password")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<String>> changeUserPassword(
            @Valid @RequestBody PasswordDto passwordDto,
            @PathVariable Long userId) throws Exception {
        userService.changeUserPassword(passwordDto, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}
