package com.ffms.resqeats.dto.usermgt.user;

import com.ffms.resqeats.dto.usermgt.role.RoleDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String userName;
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private String fax;
    private String email;
    private RoleDto role;
}
