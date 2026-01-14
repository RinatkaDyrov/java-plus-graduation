package ru.practicum.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class TwoHoursValidator implements ConstraintValidator<ValidEventDate, LocalDateTime> {
    private boolean allowNull;

    @Override
    public void initialize(ValidEventDate constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(LocalDateTime localDate, ConstraintValidatorContext constraintValidatorContext) {

        if (localDate == null) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                            constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addConstraintViolation();
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursAgo = now.plusHours(2);

        return localDate.isAfter(twoHoursAgo) || localDate.isEqual(twoHoursAgo);
    }
}
