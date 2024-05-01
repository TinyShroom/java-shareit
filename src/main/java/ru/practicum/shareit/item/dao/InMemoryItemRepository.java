package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items;
    private final Map<Long, Set<Long>> userItems;

    private long idCounter;

    public InMemoryItemRepository() {
        userItems = new HashMap<>();
        items = new HashMap<>();
    }

    @Override
    public Item create(Item item) {
        item.setId(idGenerator());
        items.put(item.getId(), item);
        userItems.putIfAbsent(item.getOwner().getId(), new HashSet<>());
        userItems.get(item.getOwner().getId()).add(item.getId());
        return item;
    }

    @Override
    public Optional<Item> findById(long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> getAll(long userId) {
        return userItems.get(userId).stream()
                .map(items::get)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> update(Item item) {
        var oldItem = items.get(item.getId());
        if (oldItem == null || !oldItem.getOwner().getId().equals(item.getOwner().getId())) {
            return Optional.empty();
        }
        updateItem(oldItem, item);
        return Optional.of(oldItem);
    }

    @Override
    public void delete(long id) {
        var item = items.remove(id);
        if (item != null) {
            userItems.get(item.getOwner().getId()).remove(id);
        }
    }

    @Override
    public List<Item> findBy(String text) {
        var pattern = Pattern.compile(".*" + text + ".*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return items.values().stream()
                .filter(item -> item.getAvailable() && (pattern.matcher(item.getName()).matches()
                        || pattern.matcher(item.getDescription()).matches()))
                .collect(Collectors.toList());
    }

    private long idGenerator() {
        return ++idCounter;
    }

    private static void updateItem(Item oldItem, Item newItem) {
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
