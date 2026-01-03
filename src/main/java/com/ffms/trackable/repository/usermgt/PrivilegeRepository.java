package com.ffms.trackable.repository.usermgt;

import com.ffms.trackable.enums.usermgt.PrivilegeType;
import com.ffms.trackable.models.usermgt.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    Optional<Privilege> findById(Integer id);

    Optional<Privilege> findByName(String privilegeName);

    List<Privilege> findAllByIdIn(Set<Long> privilegeIds);

    List<Privilege> findAllByType(PrivilegeType type);
}
