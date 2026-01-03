package com.ffms.resqeats.service.usermgt.impl;

import com.ffms.resqeats.common.service.CommonServiceImpl;
import com.ffms.resqeats.enums.usermgt.PrivilegeType;
import com.ffms.resqeats.exception.usermgt.PrivilegeAlreadyExistException;
import com.ffms.resqeats.models.usermgt.Privilege;
import com.ffms.resqeats.repository.usermgt.PrivilegeRepository;
import com.ffms.resqeats.service.usermgt.PrivilegeService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PrivilegeServiceImpl extends CommonServiceImpl<Privilege, Long, PrivilegeRepository> implements PrivilegeService {

    @Override
    public String isValid(Privilege privilege) {
        return null;
    }

    // find all privileges
    @Override
    public List<Privilege> findAllPrivileges() throws Exception {
        return Optional.ofNullable(this.findAll()).orElse(Collections.emptyList());
    }

    // find all privileges by type
    @Override
    public List<Privilege> findAllPrivilegesByType(PrivilegeType type) throws Exception {
        return Optional.ofNullable(this.repository.findAllByType(type)).orElse(Collections.emptyList());
    }

    // find all privileges by IDs
    @Override
    public List<Privilege> findPrivilegesByIds(Set<Long> privilegeIds) {
        return this.repository.findAllByIdIn(privilegeIds);
    }

    // find privileges by ID
    @Override
    public Privilege findByPrivilegeId(Long privilegeId) throws Exception {
        return this.findById(privilegeId);
    }

    // find privileges by name
    @Override
    public Optional<Privilege> findByName(final String name) {
        return this.repository.findByName(name);
    }

    // create bulk privileges
    @Override
    public List<Privilege> createPrivilegeBulk(List<Privilege> privileges) throws Exception {
        try {
            return this.repository.saveAll(privileges);
        } catch (DataIntegrityViolationException e) {
            throw new PrivilegeAlreadyExistException("one or more privileges already exist");
        }
    }

    // delete all privileges by IDs
    @Override
    public String deleteAllPrivilegesByIds(List<Long> privilegeIds) throws Exception {
        this.repository.deleteAllById(privilegeIds);
        return "privileges are deleted successfully!";
    }

    // find if privilege exists by name (from all privilege types)
    public boolean privilegeNameExists(final String privilegeName) {
        return this.findByName(privilegeName).isPresent();
    }
}
