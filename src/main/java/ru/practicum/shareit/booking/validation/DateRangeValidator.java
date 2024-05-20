package ru.practicum.shareit.booking.validation;

import ru.practicum.shareit.booking.dto.BookingCreateDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<DateRangeConstraint, BookingCreateDto> {

    @Override
    public boolean isValid(BookingCreateDto booking, ConstraintValidatorContext constraintValidatorContext) {
        return booking.getStart().isBefore(booking.getEnd());
    }
}