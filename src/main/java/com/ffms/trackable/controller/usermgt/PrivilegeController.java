package com.ffms.trackable.controller.usermgt;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.dto.usermgt.privilege.PrivilegeDto;
import com.ffms.trackable.enums.usermgt.PrivilegeType;
import com.ffms.trackable.models.usermgt.Privilege;
import com.ffms.trackable.service.usermgt.PrivilegeService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/privilege")
public class PrivilegeController {
    @Autowired
    PrivilegeService privilegeService;

    ModelMapper modelMapper;

    public PrivilegeController() {
        this.modelMapper = new ModelMapper();
    }

    // Get all privileges (SUPER_ADMIN PRIVILEGE)
    @GetMapping("/all")
    @PreAuthorize("hasPermission(#id, @appUtils.privilegeResource, @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllPrivileges() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                privilegeService.findAllPrivileges().stream().map(privilege -> modelMapper.map(privilege, PrivilegeDto.class)).toList()));
    }

    // Get 'USER' type privileges
    @GetMapping("")
    @PreAuthorize("hasPermission(#id, @appUtils.privilegeResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getUserTypePrivileges() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                privilegeService.findAllPrivilegesByType(PrivilegeType.USER).stream().map(privilege -> modelMapper.map(privilege, PrivilegeDto.class)).toList()));
    }

    // Create bulk privileges (SUPER_ADMIN PRIVILEGE)
    @PostMapping("/bulk")
    @PreAuthorize("hasPermission(#id, @appUtils.privilegeResource, @appUtils.adminPrivilege)")
    public ResponseEntity<?> createPrivilegesBulk(@Valid @RequestBody List<PrivilegeDto> privilegeDto) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                (privilegeService.createPrivilegeBulk(privilegeDto.stream().map(el -> modelMapper.map(el, Privilege.class))
                        .toList())).stream().map(el -> modelMapper.map(el, PrivilegeDto.class))));
    }

    // Delete privileges by Ids (SUPER_ADMIN PRIVILEGE)
    @DeleteMapping("")
    @PreAuthorize("hasPermission(#id, @appUtils.privilegeResource, @appUtils.adminPrivilege)")
    public ResponseEntity<?> deletePrivilegesByIds(@RequestBody List<Long> privilegeIds) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                privilegeService.deleteAllPrivilegesByIds(privilegeIds)
        ));
    }
}
