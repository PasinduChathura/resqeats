package com.ffms.trackable.repository.usermgt;

import com.ffms.trackable.enums.usermgt.RoleType;
import com.ffms.trackable.models.usermgt.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findById(Integer id);

    Optional<Role> findByName(String roleName);

    Optional<Role> findByNameAndType(String roleName, RoleType type);

    Optional<List<Role>> findAllByType(RoleType type);
}
