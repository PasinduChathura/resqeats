package com.ffms.trackable.dto.customer;

import com.ffms.trackable.enums.customer.CustomerType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentCustomerResponseDto {
    private String id;
    private String refId;
    private String name;
}
