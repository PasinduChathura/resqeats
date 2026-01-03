package com.ffms.trackable.repository.usermgt;

import java.util.List;
import java.util.Optional;

import com.ffms.trackable.common.model.Status;
import com.ffms.trackable.enums.usermgt.UserType;
import com.ffms.trackable.models.usermgt.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String username);

    Optional<User> findByEmail(String email);

    Optional<List<User>> findAllByStatus(Status status);

    Optional<List<User>> findAllByStatusAndType(Status status, UserType type);

    Optional<List<User>> findAllByType(UserType type);

}
