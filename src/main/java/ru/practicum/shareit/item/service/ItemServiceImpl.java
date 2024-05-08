package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.CommentShort;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        var owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", userId)));
        var item = itemMapper.dtoToItem(itemDto, owner);
        return itemMapper.itemToDto(itemRepository.save(item));
    }

    @Override
    public ItemDtoResponse findById(long userId, long id) {
        var item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found", id)));
        var comments = commentRepository.findAllByItemId(id)
                .stream()
                .map(commentMapper::shortToDtoResponse)
                .collect(Collectors.toList());
        if (!item.getOwner().getId().equals(userId)) {
            return itemMapper.itemsToDtoResponse(item, comments);
        }
        var bookings = bookingRepository.findBookingsShortByItem(item.getId());
        var dateTime = LocalDateTime.now();
        var last = bookings.stream()
                .filter(b -> dateTime.isAfter(b.getStart()))
                .max(Comparator.comparing(BookingDtoShort::getStart))
                .orElse(null);
        var next = bookings.stream()
                .filter(b -> dateTime.isBefore(b.getStart()))
                .min(Comparator.comparing(BookingDtoShort::getStart))
                .orElse(null);
        return itemMapper.itemsToDtoResponse(item, last, next, comments);
    }

    @Override
    public List<ItemDtoResponse> getAll(long userId) {
        var items = itemRepository.findAllByOwnerIdOrderById(userId);
        var dateTime = LocalDateTime.now();
        var bookings = bookingRepository.findAllBookingsShortByOwner(userId)
                .stream()
                .collect(Collectors.groupingBy(BookingDtoShort::getItemId,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        var lastBookings = bookings.values()
                .stream()
                .map(s -> s.stream()
                        .filter(b -> dateTime.isAfter(b.getStart()))
                        .max(Comparator.comparing(BookingDtoShort::getStart))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BookingDtoShort::getItemId, Function.identity()));

        var nextBookings = bookings.values()
                .stream()
                .map(s -> s.stream()
                        .filter(b -> dateTime.isBefore(b.getStart()))
                        .min(Comparator.comparing(BookingDtoShort::getStart))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BookingDtoShort::getItemId, Function.identity()));

        var comments = commentRepository.findAllByOwnerId(userId)
                .stream()
                .collect(Collectors.groupingBy(CommentShort::getItemId,
                        Collectors.mapping(commentMapper::shortToDtoResponse, Collectors.toList())));

        return items.stream()
                .map(item -> itemMapper.itemsToDtoResponse(item, lastBookings.get(item.getId()),
                        nextBookings.get(item.getId()), comments.getOrDefault(item.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto update(long userId, ItemDto itemDto) {
        var owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", userId)));
        var oldItem = itemRepository.findById(itemDto.getId())
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found",
                        itemDto.getId())));
        if (!oldItem.getOwner().equals(owner)) {
            throw new AccessDeniedException("only owner can update item");
        }
        itemMapper.dtoToItem(oldItem, itemDto);
        var item = itemRepository.save(oldItem);
        return itemMapper.itemToDto(item);
    }

    @Override
    public void delete(long userId, long id) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", userId)));
        var item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found", id)));
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("only owner can delete item");
        }
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isBlank()) return List.of();
        return itemMapper.itemsToDto(itemRepository.search(text));
    }

    @Override
    public CommentDtoResponse createComment(long userId, long itemId, CommentDtoRequest commentDtoRequest) {
        var author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", userId)));
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found", itemId)));
        var dateTime = LocalDateTime.now();
        if (bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId,
                BookingStatus.APPROVED, dateTime).isEmpty()) {
            throw new AccessDeniedException("you cannot create a review without booking");
        }
        var comment = commentRepository.save(commentMapper.dtoToComment(commentDtoRequest, author, item, dateTime));
        return commentMapper.commentToDto(comment);
    }
}