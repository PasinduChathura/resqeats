package com.ffms.resqeats.dto.customer;

import com.ffms.resqeats.enums.customer.CustomerType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentCustomerResponseDto {
    private String id;
    private String refId;
    private String name;
}
