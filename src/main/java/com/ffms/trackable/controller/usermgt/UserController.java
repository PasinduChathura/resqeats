package com.ffms.trackable.controller.usermgt;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.common.model.Status;
import com.ffms.trackable.dto.usermgt.password.PasswordDto;
import com.ffms.trackable.dto.usermgt.user.UserDto;
import com.ffms.trackable.dto.usermgt.user.UserRegisterDto;
import com.ffms.trackable.dto.usermgt.user.UserUpdateDto;
import com.ffms.trackable.enums.usermgt.UserType;
import com.ffms.trackable.models.usermgt.User;
import com.ffms.trackable.service.usermgt.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    ModelMapper modelMapper;

    public UserController() {
        this.modelMapper = new ModelMapper();
        modelMapper.createTypeMap(UserRegisterDto.class, User.class).addMapping(UserRegisterDto::getRole, (user, roleId) -> user.getRole().setId((Long) roleId));
        modelMapper.createTypeMap(UserUpdateDto.class, User.class).addMapping(UserUpdateDto::getRole, (user, roleId) -> user.getRole().setId((Long) roleId));
    }

    // Get all users (SUPER_ADMIN PRIVILEGE)
    @GetMapping("/all")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllUsers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                userService.findAllUsers().stream().map(user -> modelMapper.map(user, UserDto.class)).toList()));
    }

    // Get 'USER' type users
    @GetMapping("")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getUserTypeUsers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                userService.findUsersByType(UserType.USER).stream().map(user -> modelMapper.map(user, UserDto.class)).toList()));
    }

    // Get all 'ACTIVE' users
    @GetMapping("/active")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllActiveUsers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                userService.findUsersByStatusAndType(Status.ACTIVE, UserType.USER).stream().map(user -> modelMapper.map(user, UserDto.class)).toList()));
    }

    // Get all 'INACTIVE' users
    @GetMapping("/inactive")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllInactiveUsers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                userService.findUsersByStatusAndType(Status.INACTIVE, UserType.USER).stream().map(user -> modelMapper.map(user, UserDto.class)).toList()));
    }

    // Get all 'TERMINATED' users
    @GetMapping("/terminated")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllTerminatedUsers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                userService.findUsersByStatusAndType(Status.TERMINATED, UserType.USER).stream().map(user -> modelMapper.map(user, UserDto.class)).toList()));
    }

    // Get user by id
    @GetMapping("/{userId}")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(userService.findByUserId(userId), UserDto.class)));
    }

    // Create user
    @PostMapping("")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.writePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRegisterDto userDto) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(userService.createUser(modelMapper.map(userDto, User.class)), UserDto.class)));
    }

    // Update user
    @PatchMapping("/{userId}")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateDto userDto, @PathVariable Long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(userService.updateUser(modelMapper.map(userDto, User.class), userId), UserDto.class)));
    }

    // Activate user
    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(userService.changeUserStatus(Status.ACTIVE, userId), UserDto.class)
        ));
    }

    // Deactivate user
    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(userService.changeUserStatus(Status.INACTIVE, userId), UserDto.class)
        ));
    }

    // Terminate user
    @PatchMapping("/{userId}/terminate")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> terminateUser(@PathVariable Long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(userService.changeUserStatus(Status.TERMINATED, userId), UserDto.class)
        ));
    }

    // Change user password
    @PostMapping("/{userId}/password")
    @PreAuthorize("hasPermission(#id, @appUtils.userResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> changeUserPassword(@Valid @RequestBody PasswordDto passwordDto, @PathVariable Long userId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                userService.changeUserPassword(passwordDto, userId)));
    }
}
