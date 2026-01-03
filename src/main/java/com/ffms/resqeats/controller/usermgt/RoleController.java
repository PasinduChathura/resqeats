package com.ffms.resqeats.controller.usermgt;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.dto.usermgt.role.RoleCreateDto;
import com.ffms.resqeats.dto.usermgt.role.RoleDto;
import com.ffms.resqeats.dto.usermgt.role.RoleResponseDto;
import com.ffms.resqeats.dto.usermgt.role.RoleUpdateDto;
import com.ffms.resqeats.enums.usermgt.RoleType;
import com.ffms.resqeats.models.usermgt.Role;
import com.ffms.resqeats.service.usermgt.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role management controller for CRUD operations on roles.
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final ModelMapper modelMapper;

    /**
     * Get all roles (SUPER_ADMIN only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() throws Exception {
        List<RoleDto> roles = roleService.findAllRoles().stream()
                .map(role -> modelMapper.map(role, RoleDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * Get 'USER' type roles
     */
    @GetMapping
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getUserTypeRoles() throws Exception {
        List<RoleDto> roles = roleService.findRolesByType(RoleType.USER).stream()
                .map(role -> modelMapper.map(role, RoleDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * Get role by ID
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<RoleResponseDto>> getRoleById(@PathVariable Long roleId) throws Exception {
        RoleResponseDto role = modelMapper.map(roleService.findByRoleId(roleId), RoleResponseDto.class);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    /**
     * Create a new role
     */
    @PostMapping
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.writePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(@Valid @RequestBody RoleCreateDto roleDto) throws Exception {
        Role role = roleService.createRole(modelMapper.map(roleDto, Role.class));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(modelMapper.map(role, RoleResponseDto.class), "Role created successfully"));
    }

    /**
     * Update role
     */
    @PatchMapping("/{roleId}")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<RoleResponseDto>> updateRole(
            @Valid @RequestBody RoleUpdateDto roleDto,
            @PathVariable Long roleId) throws Exception {
        Role role = roleService.updateRole(modelMapper.map(roleDto, Role.class), roleId);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(role, RoleResponseDto.class), "Role updated successfully"));
    }

    /**
     * Delete role
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long roleId) throws Exception {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
    }
}
