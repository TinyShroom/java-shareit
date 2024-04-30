package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {

    Item add(Item item);

    Optional<Item> findById(long id);

    List<Item> getAll(long userId);

    Optional<Item> update(long userId, long id, Item item);

    Optional<Item> delete(long userId, long id);

    List<Item> findByText(String text);
}
