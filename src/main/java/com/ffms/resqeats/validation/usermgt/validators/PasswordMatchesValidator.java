package com.ffms.resqeats.validation.usermgt.validators;

import com.ffms.resqeats.dto.usermgt.password.PasswordDto;
import com.ffms.resqeats.validation.usermgt.annotations.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
        PasswordDto passwordDto = (PasswordDto) obj;
        return passwordDto.getPassword().equals(passwordDto.getPasswordConfirm());
    }
}
