package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        var owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", userId)));
        var item = ItemMapper.dtoToItem(itemDto, owner);
        return ItemMapper.itemToDto(itemRepository.create(item));
    }

    @Override
    public ItemDto findById(long id) {
        var item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found", id)));
        return ItemMapper.itemToDto(item);
    }

    @Override
    public List<ItemDto> getAll(long userId) {
        if (isUserNotExist(userId)) {
            throw new NotFoundException(String.format("user with id == %d not found", userId));
        }
        return ItemMapper.itemsToDto(itemRepository.getAll(userId));
    }

    @Override
    public ItemDto update(long userId, long id, ItemDto itemDto) {
        var owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", userId)));
        itemDto.setId(id);
        var item = itemRepository.update(ItemMapper.dtoToItem(itemDto, owner))
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found", id)));
        return ItemMapper.itemToDto(item);
    }

    @Override
    public void delete(long userId, long id) {
        if (isUserNotExist(userId)) {
            throw new NotFoundException(String.format("user with id == %d not found", userId));
        }
        var item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found", id)));
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("only owner can delete item");
        }
        itemRepository.delete(id);
    }

    @Override
    public List<ItemDto> findBy(String text) {
        if (text.isBlank()) return List.of();
        return ItemMapper.itemsToDto(itemRepository.findBy(text));
    }

    private boolean isUserNotExist(long userId) {
        return userRepository.findById(userId).isEmpty();
    }
}
