package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingShort;

@Mapper(componentModel = "spring")
public interface TestBookingMapper {

    @Mapping(target = "bookerId", source = "booking.booker.id")
    @Mapping(target = "itemId", source = "booking.item.id")
    BookingShort toModel(Booking booking);
}
