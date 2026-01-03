package com.ffms.trackable.validation.customer.annotations;

import com.ffms.trackable.validation.customer.validators.ParentCustomerValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ParentCustomerValidator.class})
@Documented
public @interface ValidParentCustomer {
    String message() default "parentId is required when customer type is CHILD";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}