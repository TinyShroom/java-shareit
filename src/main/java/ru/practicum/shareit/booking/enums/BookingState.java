package ru.practicum.shareit.booking.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static Optional<BookingState> parse(String name) {
        var isCorrect = Arrays.stream(values())
                .map(BookingState::name)
                .collect(Collectors.toSet())
                .contains(name);
        if (isCorrect) return Optional.of(BookingState.valueOf(name));
        return Optional.empty();
    }
}
