package com.ffms.trackable.service.usermgt;

import com.ffms.trackable.common.model.Status;
import com.ffms.trackable.common.service.CommonService;
import com.ffms.trackable.dto.usermgt.password.PasswordDto;
import com.ffms.trackable.enums.usermgt.UserType;
import com.ffms.trackable.models.usermgt.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends CommonService<User, Long> {
    List<User> findAllUsers() throws Exception;

    List<User> findUsersByType(UserType type) throws Exception;

    List<User> findUsersByStatus(Status status) throws Exception;

    List<User> findUsersByStatusAndType(Status status, UserType type) throws Exception;

    User findByUserId(Long userId) throws Exception;

    User createUser(User user) throws Exception;

    User updateUser(User user, Long userId) throws Exception;

    User changeUserStatus(Status status, Long userId) throws Exception;

    String changeUserPassword(PasswordDto passwordDto, Long userId) throws Exception;

    Optional<User> findByEmail(String email) throws Exception;

    Optional<User> findByUserName(String userName) throws Exception;

    boolean checkIfValidOldPassword(final User user, final String oldPassword) throws Exception;
}

