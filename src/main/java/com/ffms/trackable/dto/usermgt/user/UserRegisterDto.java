package com.ffms.trackable.dto.usermgt.user;

import com.ffms.trackable.validation.common.annotations.NotBlankIfNotNull;
import com.ffms.trackable.validation.usermgt.annotations.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDto {

    @NotBlank(message = "is required")
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String userName;

    @ValidEmail(message = "is invalid")
    @NotBlank(message = "is required")
    private String email;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 50, message = " must have between {min} and {max} characters")
    private String firstName;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 50, message = " must have between {min} and {max} characters")
    private String lastName;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 100, message = " must have between {min} and {max} characters")
    private String address;

    @NotBlank(message = "is required")
    @Pattern(regexp = "\\d{10}", message = "number must be 11 digits long")
    private String phone;

    @Pattern(regexp = "\\d{10}", message = "number must be 11 digits long")
    @NotBlankIfNotNull
    private String fax;

    @NotBlank(message = "is required")
    @Pattern(regexp = "\\d+", message = "must be a valid number")
    private String role;
}
