package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {

    ItemDto create(long userId, ItemDto item);

    ItemWithBookingsDto findById(long userId, long id);

    List<ItemWithBookingsDto> getAll(long userId);

    ItemDto update(long userId, ItemDto item);

    void delete(long userId, long id);

    List<ItemDto> search(String text);

    CommentDtoResponse createComment(long userId, long itemId, CommentCreateDto commentCreateDto);
}
