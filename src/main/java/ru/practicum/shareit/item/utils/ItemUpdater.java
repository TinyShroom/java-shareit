package ru.practicum.shareit.item.utils;

import ru.practicum.shareit.item.model.Item;

public class ItemUpdater {

    public static void update(Item oldItem, Item newItem) {
        if (newItem.getName() != null) {
            oldItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            oldItem.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            oldItem.setAvailable(newItem.getAvailable());
        }
    }
}
