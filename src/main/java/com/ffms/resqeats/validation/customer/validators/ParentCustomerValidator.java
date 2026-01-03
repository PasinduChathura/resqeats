package com.ffms.resqeats.validation.customer.validators;

import com.ffms.resqeats.dto.customer.CustomerCreateDto;
import com.ffms.resqeats.dto.customer.CustomerDto;
import com.ffms.resqeats.dto.customer.CustomerUpdateDto;
import com.ffms.resqeats.enums.customer.CustomerType;
import com.ffms.resqeats.validation.customer.annotations.ValidParentCustomer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ParentCustomerValidator implements ConstraintValidator<ValidParentCustomer, Object> {

    private String message;

    @Override
    public void initialize(ValidParentCustomer constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        if (obj instanceof CustomerCreateDto dto) {
            return validate(dto, context);
        }

        if (obj instanceof CustomerUpdateDto dto) {
            return validate(dto, context);
        }

        return false;
    }

    private boolean validate(CustomerDto dto, ConstraintValidatorContext context) {
        if (CustomerType.CHILD.toString().equals(dto.getType())) {
            if (dto.getParentId() != null && !dto.getParentId().isEmpty()) {
                return true;
            } else {
                addConstraintViolation(context, message);
                return false;
            }
        } else if (CustomerType.PARENT.toString().equals(dto.getType())) {
            if (dto.getParentId() == null || dto.getParentId().isEmpty()) {
                return true;
            } else {
                addConstraintViolation(context, "parentId must be null for customer type is PARENT");
                return false;
            }
        } else {
            return true;
        }
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
