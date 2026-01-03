package com.ffms.trackable.dto.customer;

import com.ffms.trackable.enums.customer.CustomerType;
import com.ffms.trackable.validation.common.annotations.NotBlankIfNotNull;
import com.ffms.trackable.validation.common.annotations.ValidDate;
import com.ffms.trackable.validation.common.annotations.ValidEnum;
import com.ffms.trackable.validation.common.annotations.ValidPhoneNumber;
import com.ffms.trackable.validation.customer.annotations.ValidParentCustomer;
import com.ffms.trackable.validation.usermgt.annotations.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidParentCustomer
public class CustomerCreateDto implements CustomerDto {
    @NotBlank(message = "is required")
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String refId;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String name;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 500, message = "must have between {min} and {max} characters")
    private String address;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 65, message = "must have between {min} and {max} characters")
    private String district;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 65, message = "must have between {min} and {max} characters")
    private String province;

    @ValidDate
    @NotBlank(message = "is required")
    private String joinDate;

    @ValidPhoneNumber
    @NotBlank(message = "is required")
    private String phone;

    @ValidPhoneNumber
    @NotBlankIfNotNull
    private String fax;

    @ValidEmail(message = "is invalid")
    @NotBlank(message = "is required")
    private String email;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 100, message = "must have between {min} and {max} characters")
    private String person;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 100, message = "must have between {min} and {max} characters")
    private String personDesignation;

    @NotBlank(message = "is required")
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String latitude;
    
    @NotBlank(message = "is required")
    @Size(min = 1, max = 50, message = "must have between {min} and {max} characters")
    private String longitude;

    @NotBlank(message = "is required")
    @ValidEnum(enumClass = CustomerType.class, message = "- invalid customer type. valid options are 'parent' and 'child")
    private String type = String.valueOf(CustomerType.PARENT);

    @NotBlankIfNotNull
    @Pattern(regexp = "\\d+", message = "must be a valid number")
    private String parentId;
}
