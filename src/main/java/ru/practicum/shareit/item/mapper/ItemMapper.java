package ru.practicum.shareit.item.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.booking.model.BookingShort;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.ItemWithRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "itemDto.name")
    @Mapping(target = "description", source = "itemDto.description")
    Item dtoToItem(ItemDto itemDto, User owner, Request request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void dtoToItem(@MappingTarget final Item item, ItemDto itemDto);

    @Mapping(target = "requestId", source = "item.request.id")
    ItemDto itemToDto(Item item);

    List<ItemDto> itemsToDto(List<Item> items);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "lastBooking", source = "last")
    @Mapping(target = "nextBooking", source = "next")
    ItemWithBookingsDto itemsToDtoResponse(Item item, BookingShort last, BookingShort next);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "lastBooking", source = "last")
    @Mapping(target = "nextBooking", source = "next")
    ItemWithBookingsDto itemsToDtoResponse(Item item, BookingShort last, BookingShort next, List<CommentDto> comments);

    ItemWithBookingsDto itemsToDtoResponse(Item item, List<CommentDto> comments);

    @Mapping(target = "requestId", source = "item.request.id")
    ItemWithRequestDto itemToDtoWithRequest(Item item);
}
