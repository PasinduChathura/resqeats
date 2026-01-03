package com.ffms.trackable.dto.usermgt.user;

import com.ffms.trackable.validation.common.annotations.NotBlankIfNotNull;
import com.ffms.trackable.validation.usermgt.annotations.ValidEmail;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    @NotBlankIfNotNull
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String userName;

    @NotBlankIfNotNull
    @ValidEmail(message = "is invalid")
    private String email;

    @NotBlankIfNotNull
    @Size(min = 1, max = 50, message = " must have between {min} and {max} characters")
    private String firstName;

    @NotBlankIfNotNull
    @Size(min = 1, max = 50, message = " must have between {min} and {max} characters")
    private String lastName;

    @NotBlankIfNotNull
    @Size(min = 1, max = 100, message = " must have between {min} and {max} characters")
    private String address;

    @NotBlankIfNotNull
    @Pattern(regexp = "\\d{10}", message = "number must be 10 digits long")
    private String phone;

    @NotBlankIfNotNull
    @Pattern(regexp = "\\d{10}", message = "number must be 10 digits long")
    private String fax;

    @NotBlankIfNotNull
    @Pattern(regexp = "\\d+", message = "must be a valid number")
    private String role;
}
