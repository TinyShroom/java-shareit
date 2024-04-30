package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.utils.ItemUpdater;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items;

    private long idCounter;

    public InMemoryItemStorage() {
        items = new HashMap<>();
    }

    @Override
    public Item add(Item item) {
        item.setId(idGenerator());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> getAll(long userId) {
        return items.values().stream()
                .filter(i -> i.getOwnerId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> update(long userId, long id, Item item) {
        var oldItem = items.get(id);
        if (oldItem == null || oldItem.getOwnerId() != userId) return Optional.empty();
        ItemUpdater.update(oldItem, item);
        return Optional.of(oldItem);
    }

    @Override
    public Optional<Item> delete(long userId, long id) {
        var item = items.get(userId);
        if (item == null || item.getOwnerId() != userId) return Optional.empty();
        return Optional.of(items.remove(id));
    }

    @Override
    public List<Item> findByText(String text) {
        var pattern = Pattern.compile(".*" + text + ".*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return items.values().stream()
                .filter(item -> item.getAvailable() && (pattern.matcher(item.getName()).matches()
                        || pattern.matcher(item.getDescription()).matches()))
                .collect(Collectors.toList());
    }

    private long idGenerator() {
        return ++idCounter;
    }
}
