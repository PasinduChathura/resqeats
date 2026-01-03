package com.ffms.resqeats.validation.common.validators;

import com.ffms.resqeats.validation.common.annotations.ValidDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValidator implements ConstraintValidator<ValidDate, String> {
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public void initialize(ValidDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(String date, ConstraintValidatorContext context) {
        if (date == null) {
            return true;
        }

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        format.setLenient(false);

        try {
            Date parsedDate = format.parse(date);
            String formattedDate = format.format(parsedDate);
            return formattedDate.equals(date);
        } catch (Exception e) {
            return false;
        }
    }
}
