package com.ffms.resqeats.service.usermgt.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffms.resqeats.common.service.CommonServiceImpl;
import com.ffms.resqeats.enums.usermgt.RoleType;
import com.ffms.resqeats.exception.usermgt.PrivilegeNotFoundException;
import com.ffms.resqeats.exception.usermgt.RoleAlreadyExistException;
import com.ffms.resqeats.exception.usermgt.RoleNotFoundException;
import com.ffms.resqeats.exception.usermgt.UserAlreadyExistException;
import com.ffms.resqeats.models.usermgt.Privilege;
import com.ffms.resqeats.models.usermgt.Role;
import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.repository.usermgt.RoleRepository;
import com.ffms.resqeats.service.usermgt.PrivilegeService;
import com.ffms.resqeats.service.usermgt.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends CommonServiceImpl<Role, Long, RoleRepository> implements RoleService {

    @Autowired
    PrivilegeService privilegeService;

    @Override
    public String isValid(Role role) {
        return null;
    }

    // find all roles
    @Override
    public List<Role> findAllRoles() throws Exception {
        return Optional.ofNullable(this.findAll())
                .orElse(Collections.emptyList());
    }

    // find all roles by type
    @Override
    public List<Role> findRolesByType(RoleType type) throws Exception {
        return this.repository.findAllByType(type)
                .orElse(Collections.emptyList());
    }

    // find role by role ID (excluding 'SUPER_ADMIN' role type)
    @Override
    public Role findByRoleId(Long roleId) throws Exception {
        Role role = this.findById(roleId);
        if (role.getType().equals(RoleType.SUPER_ADMIN)) {
            throw new RoleNotFoundException("role not found with the id: " + roleId);
        }
        return role;
    }

    // create role
    @Override
    public Role createRole(Role role) throws Exception {
        // check if role name exists
        if (this.roleNameExists(role.getName())) {
            throw new RoleAlreadyExistException("there is a role with the entered name: " + role.getName());
        }

        // validate privileges
        Set<Long> privilegeIds = role.getPrivileges().stream()
                .map(Privilege::getId)
                .collect(Collectors.toSet());

        List<Privilege> privileges = privilegeService.findPrivilegesByIds(privilegeIds);
        if (privileges.size() != privilegeIds.size()) {
            Set<Long> missingIds = privilegeIds.stream()
                    .filter(id -> privileges.stream().noneMatch(p -> p.getId().equals(id)))
                    .collect(Collectors.toSet());
            throw new PrivilegeNotFoundException("privileges not found for IDs: " + missingIds);
        }

        // create role
        return this.create(role);
    }

    // update role by ID
    @Override
    public Role updateRole(Role role, Long roleId) throws Exception {
        // check if a role is exists for role ID
        Role currentRole = this.findByRoleId(roleId);

        // check if role name already exists
        if (role.getName() != null) {
            Optional<Role> roleNameExists = this.findByName(role.getName());
            if (roleNameExists.isPresent() && !roleNameExists.get().getId().equals(roleId)) {
                throw new UserAlreadyExistException("there is a role with the entered name: " + role.getName());
            }
        }

        // validate privileges
        if (role.getPrivileges() != null && !role.getPrivileges().isEmpty()) {
            Set<Long> privilegeIds = role.getPrivileges().stream()
                    .map(Privilege::getId)
                    .collect(Collectors.toSet());

            List<Privilege> privileges = privilegeService.findPrivilegesByIds(privilegeIds);
            if (privileges.size() != privilegeIds.size()) {
                Set<Long> missingIds = privilegeIds.stream()
                        .filter(id -> privileges.stream().noneMatch(p -> p.getId().equals(id)))
                        .collect(Collectors.toSet());
                throw new PrivilegeNotFoundException("privileges not found for IDs: " + missingIds);
            }
        }

        // map dto to role
        new ObjectMapper().readerForUpdating(currentRole).readValue(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(role));

        // update role
        return this.update(currentRole, roleId);
    }

    // delete role by ID
    public String deleteRole(Long roleId) throws Exception {
        Role role = this.findByRoleId(roleId);

        if (!role.getUsers().isEmpty()) {
            List<Long> userIds = role.getUsers().stream().map(User::getId).toList();
            return "role cannot be deleted because it is assigned to users with IDs: " + userIds;
        }
        this.deleteById(roleId);
        return "role with id " + roleId + " deleted successfully";
    }

    // find role by name (from all role types)
    private Optional<Role> findByName(final String name) {
        return this.repository.findByName(name);
    }

    // find if role exists by name (from all role types)
    private boolean roleNameExists(final String roleName) {
        return this.findByName(roleName).isPresent();
    }

    // find role by name and type
    private Optional<Role> findRoleByNameAndType(final String name, final RoleType type) {
        return this.repository.findByNameAndType(name, type);
    }
}
