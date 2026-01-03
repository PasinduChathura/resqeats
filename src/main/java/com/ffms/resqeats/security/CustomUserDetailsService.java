package com.ffms.resqeats.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ffms.resqeats.models.usermgt.User;
import com.ffms.resqeats.repository.usermgt.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  @Autowired
  UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUserName(username)
        .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

    // Check if user has OAuth2 provider but trying to login with password
    if (StringUtils.hasText(user.getOauth2Provider()) && 
        (user.getPassword() == null || user.getPassword().isEmpty() || 
         user.getPassword().equals(org.springframework.security.crypto.bcrypt.BCrypt.hashpw("", org.springframework.security.crypto.bcrypt.BCrypt.gensalt())))) {
      throw new UsernameNotFoundException("User registered with " + user.getOauth2Provider() + ". Please use " + user.getOauth2Provider() + " login.");
    }

    return CustomUserDetails.build(user);
  }

}
