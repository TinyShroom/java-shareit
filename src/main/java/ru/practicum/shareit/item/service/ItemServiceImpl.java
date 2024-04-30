package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.dao.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto add(long userId, ItemDto itemDto) {
        if (isUserNotExist(userId)) {
            throw new NotFoundException(String.format("user with id == %d not found", userId));
        }
        var item = ItemMapper.dtoToItem(itemDto, userId);
        return ItemMapper.itemToDto(itemStorage.add(item));
    }

    @Override
    public ItemDto findById(long id) {
        var item = itemStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found", id)));
        return ItemMapper.itemToDto(item);
    }

    @Override
    public List<ItemDto> getAll(long userId) {
        if (isUserNotExist(userId)) {
            throw new NotFoundException(String.format("user with id == %d not found", userId));
        }
        return ItemMapper.itemsToDto(itemStorage.getAll(userId));
    }

    @Override
    public ItemDto update(long userId, long id, ItemDto itemDto) {
        if (isUserNotExist(userId)) {
            throw new NotFoundException(String.format("user with id == %d not found", userId));
        }
        var item = itemStorage.update(userId, id, ItemMapper.dtoToItem(itemDto, userId))
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found", id)));
        return ItemMapper.itemToDto(item);
    }

    @Override
    public void delete(long userId, long id) {
        if (isUserNotExist(userId)) {
            throw new NotFoundException(String.format("user with id == %d not found", userId));
        }
        itemStorage.delete(userId, id)
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found", id)));
    }

    @Override
    public List<ItemDto> findByText(String text) {
        if (text.isBlank()) return List.of();
        return ItemMapper.itemsToDto(itemStorage.findByText(text));
    }

    private boolean isUserNotExist(long userId) {
        return userStorage.findById(userId).isEmpty();
    }
}
