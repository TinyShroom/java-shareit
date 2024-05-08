package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Item create(Item item);

    Optional<Item> findById(long id);

    List<Item> getAll(long userId);

    Optional<Item> update(Item item);

    void delete(long id);

    List<Item> findBy(String text);
}
