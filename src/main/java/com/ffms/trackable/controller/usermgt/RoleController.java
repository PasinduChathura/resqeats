package com.ffms.trackable.controller.usermgt;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.dto.usermgt.role.RoleCreateDto;
import com.ffms.trackable.dto.usermgt.role.RoleDto;
import com.ffms.trackable.dto.usermgt.role.RoleResponseDto;
import com.ffms.trackable.dto.usermgt.role.RoleUpdateDto;
import com.ffms.trackable.enums.usermgt.RoleType;
import com.ffms.trackable.models.usermgt.Privilege;
import com.ffms.trackable.models.usermgt.Role;
import com.ffms.trackable.service.usermgt.RoleService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    RoleService roleService;

    ModelMapper modelMapper;

    public RoleController() {
        this.modelMapper = new ModelMapper();
        modelMapper.createTypeMap(RoleCreateDto.class, Role.class).addMappings(mapper -> mapper.using(ctx -> ((List<String>) ctx.getSource()).stream().map(Long::valueOf).map(Privilege::new).collect(Collectors.toSet())).map(RoleCreateDto::getPrivileges, Role::setPrivileges));
        modelMapper.createTypeMap(RoleUpdateDto.class, Role.class).addMappings(mapper -> mapper.using(ctx -> {
            List<String> privileges = (List<String>) ctx.getSource();
            return (privileges != null && !privileges.isEmpty()) ? privileges.stream().map(Long::valueOf).map(Privilege::new).collect(Collectors.toSet()) : null;
        }).map(RoleUpdateDto::getPrivileges, Role::setPrivileges));
    }

    // Get all roles (SUPER_ADMIN PRIVILEGE)
    @GetMapping("/all")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllRoles() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                roleService.findAllRoles().stream().map(role -> modelMapper.map(role, RoleDto.class)).toList()));
    }

    // Get 'USER' type roles
    @GetMapping("")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getUserTypeRoles() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                roleService.findRolesByType(RoleType.USER).stream().map(role -> modelMapper.map(role, RoleDto.class)).toList()));
    }

    // Get role by id
    @GetMapping("/{roleId}")
//    @PreAuthorize("hasPermission(#id, 'role', 'read,administration')")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getRoleById(@PathVariable Long roleId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(roleService.findByRoleId(roleId), RoleResponseDto.class)));
    }

    // Create role
    @PostMapping("")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.writePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleCreateDto roleDto) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(roleService.createRole(modelMapper.map(roleDto, Role.class)), RoleResponseDto.class)));
    }

    // Update role
    @PatchMapping("/{roleId}")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> updateRole(@Valid @RequestBody RoleUpdateDto RoleDto, @PathVariable Long roleId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(roleService.updateRole(modelMapper.map(RoleDto, Role.class), roleId), RoleResponseDto.class)));
    }

    // Delete role
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasPermission(#id, @appUtils.roleResource, @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> deleteRole(@PathVariable Long roleId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                roleService.deleteRole(roleId)
        ));
    }
}
