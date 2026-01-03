package com.ffms.resqeats.controller.usermgt;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.dto.usermgt.privilege.PrivilegeDto;
import com.ffms.resqeats.enums.usermgt.PrivilegeType;
import com.ffms.resqeats.models.usermgt.Privilege;
import com.ffms.resqeats.service.usermgt.PrivilegeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Privilege management controller for CRUD operations on privileges.
 */
@RestController
@RequestMapping("/privileges")
@RequiredArgsConstructor
public class PrivilegeController {

    private final PrivilegeService privilegeService;
    private final ModelMapper modelMapper;

    /**
     * Get all privileges (SUPER_ADMIN only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasPermission(#id, @appUtils.privilegeResource, @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<PrivilegeDto>>> getAllPrivileges() throws Exception {
        List<PrivilegeDto> privileges = privilegeService.findAllPrivileges().stream()
                .map(privilege -> modelMapper.map(privilege, PrivilegeDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(privileges));
    }

    /**
     * Get 'USER' type privileges
     */
    @GetMapping
    @PreAuthorize("hasPermission(#id, @appUtils.privilegeResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<PrivilegeDto>>> getUserTypePrivileges() throws Exception {
        List<PrivilegeDto> privileges = privilegeService.findAllPrivilegesByType(PrivilegeType.USER).stream()
                .map(privilege -> modelMapper.map(privilege, PrivilegeDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(privileges));
    }

    /**
     * Create bulk privileges (SUPER_ADMIN only)
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasPermission(#id, @appUtils.privilegeResource, @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<PrivilegeDto>>> createPrivilegesBulk(
            @Valid @RequestBody List<PrivilegeDto> privilegeDto) throws Exception {
        List<Privilege> created = privilegeService.createPrivilegeBulk(
                privilegeDto.stream()
                        .map(el -> modelMapper.map(el, Privilege.class))
                        .toList()
        );
        List<PrivilegeDto> result = created.stream()
                .map(el -> modelMapper.map(el, PrivilegeDto.class))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Privileges created successfully"));
    }

    /**
     * Delete privileges by IDs (SUPER_ADMIN only)
     */
    @DeleteMapping
    @PreAuthorize("hasPermission(#id, @appUtils.privilegeResource, @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<Void>> deletePrivilegesByIds(@RequestBody List<Long> privilegeIds) throws Exception {
        privilegeService.deleteAllPrivilegesByIds(privilegeIds);
        return ResponseEntity.ok(ApiResponse.success(null, "Privileges deleted successfully"));
    }
}
