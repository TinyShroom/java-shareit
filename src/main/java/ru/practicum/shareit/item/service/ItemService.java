package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto add(long userId, ItemDto item);

    ItemDto findById(long id);

    List<ItemDto> getAll(long userId);

    ItemDto update(long userId, long id, ItemDto item);

    void delete(long userId, long id);

    List<ItemDto> findByText(String text);
}
