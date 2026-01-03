package com.ffms.trackable.service.usermgt.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffms.trackable.common.dto.MailDto;
import com.ffms.trackable.common.model.Status;
import com.ffms.trackable.common.service.CommonServiceImpl;
import com.ffms.trackable.dto.usermgt.password.PasswordDto;
import com.ffms.trackable.enums.usermgt.UserType;
import com.ffms.trackable.exception.usermgt.UserAlreadyExistException;
import com.ffms.trackable.exception.usermgt.UserNotFoundException;
import com.ffms.trackable.models.usermgt.Role;
import com.ffms.trackable.models.usermgt.User;
import com.ffms.trackable.repository.usermgt.UserRepository;
import com.ffms.trackable.service.usermgt.RoleService;
import com.ffms.trackable.service.usermgt.UserService;
import com.ffms.trackable.util.CustomPasswordGenerator;
import com.ffms.trackable.util.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl extends CommonServiceImpl<User, Long, UserRepository> implements UserService {

    @Autowired
    EmailSender emailSender;
    @Autowired
    RoleService roleService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String isValid(User user) {
        return null;
    }

    // find all users
    @Override
    public List<User> findAllUsers() throws Exception {
        return Optional.ofNullable(this.findAll())
                .orElse(Collections.emptyList());
    }

    // find all users by type
    @Override
    public List<User> findUsersByType(UserType type) throws Exception {
        return this.repository.findAllByType(type)
                .orElse(Collections.emptyList());
    }

    // find all users by status
    @Override
    public List<User> findUsersByStatus(Status status) throws Exception {
        return this.repository.findAllByStatus(status)
                .orElse(Collections.emptyList());
    }

    // find all users by status and type
    @Override
    public List<User> findUsersByStatusAndType(Status status, UserType type) throws Exception {
        return this.repository.findAllByStatusAndType(status, type)
                .orElse(Collections.emptyList());
    }

    // find user by user ID (excluding 'SUPER_ADMIN' user type)
    @Override
    public User findByUserId(Long userId) throws Exception {
        User user = this.findById(userId);
        if (user.getType().equals(UserType.SUPER_ADMIN)) {
            throw new UserNotFoundException("user not found with the id: " + userId);
        }
        return user;
    }

    // create user
    @Override
    public User createUser(User user) throws Exception {
        // check if userName already exists
        if (this.usernameExists(user.getUserName())) {
            throw new UserAlreadyExistException("there is a user with that user name: " + user.getUserName());
        }

        // check if email already exists
        if (this.emailExists(user.getEmail())) {
            throw new UserAlreadyExistException("there is a user with that email: " + user.getEmail());
        }

        // generate temporary random password
        final String generatedPassword = CustomPasswordGenerator.generatePassayPassword();
        user.setPassword(passwordEncoder.encode(generatedPassword));

        // find role by role ID
        Role role = roleService.findByRoleId(user.getRole().getId());
        user.setRole(role);

        // create user
        User createdUser = this.create(user);

        // send user registration email to created user
        this.sendUserRegistrationEmail(createdUser, generatedPassword);
        return createdUser;
    }

    @Override
    public User updateUser(User user, Long userId) throws Exception {
        // check if a user is exists for the user ID
        User currentUser = this.findByUserId(userId);

        // check if userName already exists
        if (user.getUserName() != null) {
            Optional<User> userNameExists = this.findByUserName(user.getUserName());
            if (userNameExists.isPresent() && !userNameExists.get().getId().equals(userId)) {
                throw new UserAlreadyExistException("there is a user with that user name: " + user.getUserName());
            }
        }

        // check if email already exists
        if (user.getEmail() != null) {
            Optional<User> emailExists = this.findByEmail(user.getEmail());
            if (emailExists.isPresent() && !emailExists.get().getId().equals(userId)) {
                throw new UserAlreadyExistException("there is a user with that email: " + user.getEmail());
            }
        }

        // find role by role ID
        if(user.getRole() != null && user.getRole().getId() != null){
            Role role = roleService.findByRoleId(user.getRole().getId());
            user.setRole(role);
        }

        // map dto to user
        new ObjectMapper().readerForUpdating(currentUser).readValue(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(user));

        // update user
        return this.update(currentUser, userId);
    }

    // change user status
    @Override
    public User changeUserStatus(Status status, Long userId) throws Exception {
        User user = this.findByUserId(userId);
        user.setStatus(status);
        return this.update(user, userId);
    }

    // change user password
    @Override
    public String changeUserPassword(PasswordDto passwordDto, Long userId) throws Exception {
        final User user = this.findByUserId(userId);
        user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
        this.update(user, userId);
        return "password updated successfully!";
    }

    // check if old password matches
    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    // find user by email (from all user types)
    public Optional<User> findByEmail(final String email) {
        return this.repository.findByEmail(email);
    }

    // find user by userName (from all user types)
    public Optional<User> findByUserName(final String username) {
        return this.repository.findByUserName(username);
    }

    // find if user exists by email (from all user types)
    private boolean emailExists(final String email) {
        return this.findByEmail(email).isPresent();
    }

    // find if user exists by userName (from all user types)
    private boolean usernameExists(final String username) {
        return this.findByUserName(username).isPresent();
    }

    // send user registration email
    @Async
    private void sendUserRegistrationEmail(User user, String password) throws Exception {
        final String from = "Trackable";
        final String subject = "Trackable User Credentials (CONFIDENTIAL)";
        final String body = "Please use following system generated credentials for your initial login. You are advised to change the system generated password on change password screen \n\n" + "Username : " + user.getUserName() + '\n' + "Password : " + password;
        MailDto mailDto = new MailDto();
        mailDto.setSubject(subject);
        mailDto.setBody(body);
        mailDto.setFrom(from);
        mailDto.setTo(EmailSender.commaSeparatedStringToArray(user.getEmail()));
        mailDto.setCc(EmailSender.commaSeparatedStringToArray(null));
        mailDto.setBcc(EmailSender.commaSeparatedStringToArray(null));
        emailSender.sendEmail(mailDto);
    }
}