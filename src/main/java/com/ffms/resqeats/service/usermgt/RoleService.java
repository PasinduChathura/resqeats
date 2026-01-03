package com.ffms.resqeats.service.usermgt;

import com.ffms.resqeats.common.service.CommonService;
import com.ffms.resqeats.enums.usermgt.RoleType;
import com.ffms.resqeats.models.usermgt.Role;

import java.util.List;

public interface RoleService extends CommonService<Role, Long> {
    List<Role> findAllRoles() throws Exception;

    List<Role> findRolesByType(RoleType type) throws Exception;

    Role findByRoleId(Long roleId) throws Exception;

    Role createRole(Role role) throws Exception;

    Role updateRole(Role role, Long roleId) throws Exception;

    String deleteRole(Long roleId) throws Exception;
}