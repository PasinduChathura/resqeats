package com.ffms.trackable.validation.usermgt.validators;

import com.ffms.trackable.dto.usermgt.password.PasswordDto;
import com.ffms.trackable.validation.usermgt.annotations.PasswordMatches;
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
