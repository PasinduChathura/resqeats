package com.ffms.trackable.dto.usermgt.role;

import com.ffms.trackable.dto.usermgt.privilege.PrivilegeDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleResponseDto extends RoleDto {
    private List<PrivilegeDto> privileges;
}
