package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
public class BookingDtoResponse {
    private Long id;
    private BookerDto booker;
    private ItemDtoIdAndName item;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}
