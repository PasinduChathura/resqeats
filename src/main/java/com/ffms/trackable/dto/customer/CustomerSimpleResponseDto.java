package com.ffms.trackable.dto.customer;

import com.ffms.trackable.enums.customer.CustomerType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CustomerSimpleResponseDto {
    private String id;
    private String refId;
    private String name;
    private String address;
    private String district;
    private String province;
    private String phone;
    private String email;
    private String person;
    private CustomerType type;
}
