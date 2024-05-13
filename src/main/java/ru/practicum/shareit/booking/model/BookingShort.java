package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingShort {
    private Long id;
    private Long itemId;
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
}
