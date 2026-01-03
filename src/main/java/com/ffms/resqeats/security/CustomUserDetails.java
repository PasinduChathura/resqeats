package com.ffms.resqeats.security;

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

import com.ffms.resqeats.models.usermgt.User;
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
        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        
        // Add role-based authority (ROLE_USER, ROLE_SHOP_OWNER, ROLE_ADMIN, etc.)
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getType().getName()));
            
            // Add privilege-based authorities
            if (user.getRole().getPrivileges() != null) {
                user.getRole().getPrivileges().forEach(privilege ->
                        authorities.add(new SimpleGrantedAuthority(privilege.getName())));
            }
        } else {
            // Default role for users without assigned role
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new CustomUserDetails(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getRole() != null ? user.getRole().getId().intValue() : 0);
    }

    public static CustomUserDetails build(User user, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        
        // Add role-based authority (ROLE_USER, ROLE_SHOP_OWNER, ROLE_ADMIN, etc.)
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getType().getName()));
            
            // Add privilege-based authorities
            if (user.getRole().getPrivileges() != null) {
                user.getRole().getPrivileges().forEach(privilege ->
                        authorities.add(new SimpleGrantedAuthority(privilege.getName())));
            }
        } else {
            // Default role for users without assigned role
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new CustomUserDetails(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getRole() != null ? user.getRole().getId().intValue() : 0,
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

    public Long getUserId() {
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
