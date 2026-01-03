package com.ffms.trackable.dto.customer;

import com.ffms.trackable.enums.customer.CustomerType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CustomerResponseDto {
    private String id;
    private String refId;
    private String name;
    private String address;
    private String district;
    private String province;
    private Date joinDate;
    private String phone;
    private String fax;
    private String email;
    private String person;
    private String personDesignation;
    private CustomerType type;
    private ParentCustomerResponseDto parent;
    private String latitude;
    private String longitude;
}
