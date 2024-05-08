package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.util.List;

public interface ItemService {

    ItemDto create(long userId, ItemDto item);

    ItemDtoResponse findById(long userId, long id);

    List<ItemDtoResponse> getAll(long userId);

    ItemDto update(long userId, ItemDto item);

    void delete(long userId, long id);

    List<ItemDto> search(String text);

    CommentDtoResponse createComment(long userId, long itemId, CommentDtoRequest commentDtoRequest);
}
