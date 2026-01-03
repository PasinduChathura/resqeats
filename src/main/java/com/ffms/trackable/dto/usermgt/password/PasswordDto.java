package com.ffms.trackable.dto.usermgt.password;

import com.ffms.trackable.validation.usermgt.annotations.PasswordMatches;
import com.ffms.trackable.validation.usermgt.annotations.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@PasswordMatches
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordDto {

    @ValidPassword(message = "is invalid")
    @NotBlank(message = "is required")
    private String password;

    @NotBlank(message = "is required")
    private String passwordConfirm;
}
