package com.ffms.resqeats.repository.usermgt;

import java.util.List;
import java.util.Optional;

import com.ffms.resqeats.common.model.Status;
import com.ffms.resqeats.enums.usermgt.UserType;
import com.ffms.resqeats.models.usermgt.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String username);

    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role r LEFT JOIN FETCH r.privileges WHERE u.email = :email")
    Optional<User> findByEmailWithRoleAndPrivileges(@Param("email") String email);

    Optional<List<User>> findAllByStatus(Status status);

    Optional<List<User>> findAllByStatusAndType(Status status, UserType type);

    Optional<List<User>> findAllByType(UserType type);

    boolean existsByUserName(String username);

    boolean existsByEmail(String email);
}
