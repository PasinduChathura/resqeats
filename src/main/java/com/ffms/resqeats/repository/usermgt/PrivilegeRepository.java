package com.ffms.resqeats.repository.usermgt;

import com.ffms.resqeats.enums.usermgt.PrivilegeType;
import com.ffms.resqeats.models.usermgt.Privilege;
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
