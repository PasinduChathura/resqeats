package com.ffms.trackable.service.usermgt;

import com.ffms.trackable.common.service.CommonService;
import com.ffms.trackable.enums.usermgt.RoleType;
import com.ffms.trackable.models.usermgt.Role;

import java.util.List;

public interface RoleService extends CommonService<Role, Long> {
    List<Role> findAllRoles() throws Exception;

    List<Role> findRolesByType(RoleType type) throws Exception;

    Role findByRoleId(Long roleId) throws Exception;

    Role createRole(Role role) throws Exception;

    Role updateRole(Role role, Long roleId) throws Exception;

    String deleteRole(Long roleId) throws Exception;
}