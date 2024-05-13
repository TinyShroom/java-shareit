package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;

import java.util.List;

public interface BookingService {

    BookingDto create(BookingCreateDto item);

    BookingDto findById(long id, Long userId);

    BookingDto updateStatus(long id, Long ownerId, boolean approved);

    List<BookingDto> findAllForUser(Long bookerId, BookingState state);

    List<BookingDto> findAllForOwner(Long ownerId, BookingState state);
}
