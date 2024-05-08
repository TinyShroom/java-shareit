package ru.practicum.shareit.item.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "itemDto.name")
    Item dtoToItem(ItemDto itemDto, User owner);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void dtoToItem(@MappingTarget final Item item, ItemDto itemDto);

    ItemDto itemToDto(Item item);

    List<ItemDto> itemsToDto(List<Item> items);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "lastBooking", source = "last")
    @Mapping(target = "nextBooking", source = "next")
    ItemDtoResponse itemsToDtoResponse(Item item, BookingDtoShort last, BookingDtoShort next);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "lastBooking", source = "last")
    @Mapping(target = "nextBooking", source = "next")
    ItemDtoResponse itemsToDtoResponse(Item item, BookingDtoShort last, BookingDtoShort next, List<CommentDtoResponse> comments);

    ItemDtoResponse itemsToDtoResponse(Item item, List<CommentDtoResponse> comments);
}
