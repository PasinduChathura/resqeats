package com.ffms.trackable.service.usermgt;

import com.ffms.trackable.common.service.CommonService;
import com.ffms.trackable.enums.usermgt.PrivilegeType;
import com.ffms.trackable.models.usermgt.Privilege;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PrivilegeService extends CommonService<Privilege, Long> {
    List<Privilege> findAllPrivileges() throws Exception;

    List<Privilege> findAllPrivilegesByType(PrivilegeType type) throws Exception;

    List<Privilege> findPrivilegesByIds(Set<Long> privilegeIds) throws Exception;

    Privilege findByPrivilegeId(Long privilegeId) throws Exception;

    List<Privilege> createPrivilegeBulk(List<Privilege> privilege) throws Exception;

    String deleteAllPrivilegesByIds(List<Long> privilegeIds) throws Exception;

    Optional<Privilege> findByName(final String name) throws Exception;

    boolean privilegeNameExists(final String privilegeName) throws Exception;
}