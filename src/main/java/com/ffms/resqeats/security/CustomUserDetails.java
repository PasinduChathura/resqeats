package com.ffms.resqeats.security;

import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ffms.resqeats.user.entity.User;
import com.ffms.resqeats.user.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Spring Security UserDetails implementation per SRS Section 6.2.
 * Uses UUID for user identification and enum-based RBAC.
 */
public class CustomUserDetails implements UserDetails, OAuth2User {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String email;
    private String phone;
    private String role;
    private UUID merchantId;
    private UUID outletId;
    private UserStatus status;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public CustomUserDetails(UUID id, String email, String phone, String password,
                             Collection<? extends GrantedAuthority> authorities, 
                             String role, UUID merchantId, UUID outletId, UserStatus status) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.authorities = authorities;
        this.role = role;
        this.merchantId = merchantId;
        this.outletId = outletId;
        this.status = status;
    }

    public CustomUserDetails(UUID id, String email, String phone, String password,
                             Collection<? extends GrantedAuthority> authorities, 
                             String role, UUID merchantId, UUID outletId, UserStatus status,
                             Map<String, Object> attributes) {
        this(id, email, phone, password, authorities, role, merchantId, outletId, status);
        this.attributes = attributes;
    }

    public static CustomUserDetails build(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role-based authority (ROLE_ADMIN, ROLE_MERCHANT, ROLE_OUTLET_USER, ROLE_USER)
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getPasswordHash(),
                authorities,
                user.getRole() != null ? user.getRole().name() : "USER",
                user.getMerchantId(),
                user.getOutletId(),
                user.getStatus());
    }

    public static CustomUserDetails build(User user, Map<String, Object> attributes) {
        CustomUserDetails details = build(user);
        details.attributes = attributes;
        return details;
    }

    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : new HashMap<>();
    }

    @Override
    public String getName() {
        return email != null ? email : phone;
    }

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public UUID getOutletId() {
        return outletId;
    }

    @Override
    public String getUsername() {
        return email != null ? email : phone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails user = (CustomUserDetails) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
