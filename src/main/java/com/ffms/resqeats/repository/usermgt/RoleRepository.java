package com.ffms.resqeats.repository.usermgt;

import com.ffms.resqeats.enums.usermgt.RoleType;
import com.ffms.resqeats.models.usermgt.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findById(Integer id);

    Optional<Role> findByName(String roleName);

    Optional<Role> findByNameAndType(String roleName, RoleType type);

    Optional<List<Role>> findAllByType(RoleType type);

    Optional<Role> findByType(RoleType type);
    
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.privileges WHERE r.type = :type")
    Optional<Role> findByTypeWithPrivileges(@Param("type") RoleType type);
}
