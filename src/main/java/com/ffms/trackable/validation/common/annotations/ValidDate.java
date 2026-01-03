package com.ffms.trackable.validation.common.annotations;

import com.ffms.trackable.validation.common.validators.DateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = DateValidator.class)
@Documented
public @interface ValidDate {
    String message() default "Invalid date time. Please enter date in 'yyyy-MM-dd' format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
