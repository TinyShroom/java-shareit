package ru.practicum.shareit.booking.validation;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<DateRangeConstraint, BookingDtoRequest> {

    @Override
    public boolean isValid(BookingDtoRequest booking, ConstraintValidatorContext constraintValidatorContext) {
        return booking.getStart().isBefore(booking.getEnd());
    }
}