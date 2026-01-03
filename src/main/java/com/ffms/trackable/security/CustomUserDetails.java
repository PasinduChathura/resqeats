package com.ffms.trackable.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ffms.trackable.models.usermgt.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CustomUserDetails implements UserDetails, OAuth2User {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String email;

    private Integer role;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    private Map<String, Object> attributes;

    public CustomUserDetails(Long id, String username, String email, String password,
                             Collection<? extends GrantedAuthority> authorities, Integer role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.role = role;
    }

    public CustomUserDetails(Long id, String username, String email, String password,
                             Collection<? extends GrantedAuthority> authorities, Integer role, 
                             Map<String, Object> attributes) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.role = role;
        this.attributes = attributes;
    }

    public static CustomUserDetails build(User user) {
        List<GrantedAuthority> authorities = user.getRole().getPrivileges().stream()
                .map(privilege -> new SimpleGrantedAuthority(privilege.getName()))
                .collect(Collectors.toList());

        return new CustomUserDetails(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getRole().getId().intValue());
    }

    public static CustomUserDetails build(User user, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities = user.getRole().getPrivileges().stream()
                .map(privilege -> new SimpleGrantedAuthority(privilege.getName()))
                .collect(Collectors.toList());

        return new CustomUserDetails(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getRole().getId().intValue(),
                attributes);
    }

    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return username;
    }

    // UserDetails methods
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public Integer getRole() {
        return role;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CustomUserDetails user = (CustomUserDetails) o;
        return Objects.equals(id, user.id);
    }
}
