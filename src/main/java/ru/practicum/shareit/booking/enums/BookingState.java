package ru.practicum.shareit.booking.enums;

import java.util.Arrays;
import java.util.Optional;

public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static Optional<BookingState> parse(String name) {
        return Arrays.stream(values())
                .filter(b -> b.name().equals(name))
                .findFirst();
    }
}
