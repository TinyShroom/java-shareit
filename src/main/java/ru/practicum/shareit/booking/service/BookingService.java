package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.util.List;

public interface BookingService {

    BookingDtoResponse create(BookingDtoRequest item);

    BookingDtoResponse findById(long id, Long userId);

    BookingDtoResponse updateStatus(long id, Long ownerId, boolean approved);

    List<BookingDtoResponse> findAllForUser(Long bookerId, String state);

    List<BookingDtoResponse> findAllForOwner(Long ownerId, String state);
}
