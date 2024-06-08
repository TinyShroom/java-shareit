package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private Long id;
    private BookerDto booker;
    private ItemShortDto item;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}
