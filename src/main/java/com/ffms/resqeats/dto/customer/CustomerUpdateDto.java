package com.ffms.resqeats.dto.customer;

import com.ffms.resqeats.enums.customer.CustomerType;
import com.ffms.resqeats.validation.common.annotations.NotBlankIfNotNull;
import com.ffms.resqeats.validation.common.annotations.ValidDate;
import com.ffms.resqeats.validation.common.annotations.ValidEnum;
import com.ffms.resqeats.validation.common.annotations.ValidPhoneNumber;
import com.ffms.resqeats.validation.customer.annotations.ValidParentCustomer;
import com.ffms.resqeats.validation.usermgt.annotations.ValidEmail;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidParentCustomer
public class CustomerUpdateDto implements CustomerDto {
    @NotBlankIfNotNull
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String refId;

    @NotBlankIfNotNull
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String name;

    @NotBlankIfNotNull
    @Size(min = 1, max = 500, message = "must have between {min} and {max} characters")
    private String address;

    @NotBlankIfNotNull
    @Size(min = 1, max = 65, message = "must have between {min} and {max} characters")
    private String district;

    @NotBlankIfNotNull
    @Size(min = 1, max = 65, message = "must have between {min} and {max} characters")
    private String province;

    @ValidDate
    @NotBlankIfNotNull
    private String joinDate;

    @ValidPhoneNumber
    @NotBlankIfNotNull
    private String phone;

    @ValidPhoneNumber
    @NotBlankIfNotNull
    private String fax;

    @ValidEmail(message = "is invalid")
    @NotBlankIfNotNull
    private String email;

    @NotBlankIfNotNull
    @Size(min = 1, max = 100, message = "must have between {min} and {max} characters")
    private String person;

    @NotBlankIfNotNull
    @Size(min = 1, max = 100, message = "must have between {min} and {max} characters")
    private String personDesignation;

    @NotBlankIfNotNull
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String latitude;

    @NotBlankIfNotNull
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String longitude;

    @NotBlankIfNotNull
    @ValidEnum(enumClass = CustomerType.class, message = "- invalid customer type. valid options are 'parent' and 'child'")
    private String type = String.valueOf(CustomerType.PARENT);

    @NotBlankIfNotNull
    @Pattern(regexp = "\\d+", message = "must be a valid number")
    private String parentId;
}
