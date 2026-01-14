package ru.practicum.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

//валидатор даты начала мероприятия не ранее чем за 2 часа от now

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = TwoHoursValidator.class)
public @interface ValidEventDate {
    String message() default "Дата должна быть не раньше чем за 2 часа от текущего времени";

    Class<?>[] groups() default {};

    boolean allowNull() default false;

    Class<? extends Payload>[] payload() default {};
}
