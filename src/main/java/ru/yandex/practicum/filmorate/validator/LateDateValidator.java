package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LateDateValidator implements ConstraintValidator<LateDate, LocalDate> {
    DateTimeFormatter dateTimeFormatter;
    private LocalDate checkDate;

    public void initialize(LateDate constraint) {
        dateTimeFormatter = DateTimeFormatter.ofPattern(constraint.formatter());
        checkDate = LocalDate.parse(constraint.day(), dateTimeFormatter);
    }

    public boolean isValid(LocalDate localDate, ConstraintValidatorContext context) {
        return localDate != null && !localDate.isBefore(checkDate);
    }
}
