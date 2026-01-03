package com.ffms.trackable.validation.common.annotations;

import com.ffms.trackable.validation.common.validators.NotBlankIfNotNullValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NotBlankIfNotNullValidator.class)
@Documented
public @interface NotBlankIfNotNull {
    String message() default "is required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
