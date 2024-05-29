package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.BookingShort;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.CommentShort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto create(long userId, ItemDto itemDto) {
        var owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        Request request = null;
        if (itemDto.getRequestId() != null) {
            request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessages.REQUEST_NOT_FOUND.getFormatMessage(
                            itemDto.getRequestId())));
        }
        var item = itemMapper.toModel(itemDto, owner, request);
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemWithBookingsDto findById(long userId, long id) {
        var item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(
                        id)));
        var comments = commentRepository.findAllByItemId(id)
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
        if (!item.getOwner().getId().equals(userId)) {
            return itemMapper.toItemWithBookingsDto(item, comments);
        }
        var bookings = bookingRepository.findBookingsShortByItem(item.getId());
        var dateTime = LocalDateTime.now();
        var last = bookings.stream()
                .filter(b -> dateTime.isAfter(b.getStart()))
                .max(Comparator.comparing(BookingShort::getStart))
                .orElse(null);
        var next = bookings.stream()
                .filter(b -> dateTime.isBefore(b.getStart()))
                .min(Comparator.comparing(BookingShort::getStart))
                .orElse(null);
        return itemMapper.toItemWithBookingsDto(item, last, next, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemWithBookingsDto> getAll(long userId, Pageable pageable) {
        List<Item> items;
        items = itemRepository.findAllByOwnerId(userId, pageable);
        var dateTime = LocalDateTime.now();
        var itemsId = items.stream().map(Item::getId).collect(Collectors.toList());
        var bookings = bookingRepository.findAllBookingsShortByItemIdIn(itemsId, Sort.by("start").descending());
        Map<Long, BookingShort> lastBookings = new HashMap<>();
        Map<Long, BookingShort> nextBookings = new HashMap<>();
        for (var booking: bookings) {
            if (dateTime.isAfter(booking.getStart())) {
                lastBookings.putIfAbsent(booking.getItemId(), booking);
            } else {
                nextBookings.put(booking.getItemId(), getNextBooking(nextBookings.get(booking.getItemId()), booking));
            }
        }

        var comments = commentRepository.findAllByItemIdIn(itemsId)
                .stream()
                .collect(Collectors.groupingBy(CommentShort::getItemId,
                        Collectors.mapping(commentMapper::toDto, Collectors.toList())));

        return items.stream()
                .map(item -> itemMapper.toItemWithBookingsDto(item, lastBookings.get(item.getId()),
                        nextBookings.get(item.getId()), comments.getOrDefault(item.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto update(long userId, ItemDto itemDto) {
        var owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var oldItem = itemRepository.findById(itemDto.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(
                        itemDto.getId())));
        if (!oldItem.getOwner().equals(owner)) {
            throw new AccessDeniedException(ErrorMessages.OWNER_UPDATE.getMessage());
        }
        itemMapper.toModel(oldItem, itemDto);
        var item = itemRepository.save(oldItem);
        return itemMapper.toDto(item);
    }

    @Override
    @Transactional
    public void delete(long userId, long id) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(
                        id)));
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException(ErrorMessages.OWNER_DELETE.getMessage());
        }
        itemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text, Pageable pageable) {
        if (text.isBlank()) return List.of();
        return itemMapper.toDto(itemRepository.search(text, pageable));
    }

    @Override
    @Transactional
    public CommentDto createComment(long userId, long itemId, CommentCreateDto commentCreateDto) {
        var author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(itemId)));
        var dateTime = LocalDateTime.now();
        if (bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId,
                BookingStatus.APPROVED, dateTime).isEmpty()) {
            throw new AccessDeniedException(ErrorMessages.REVIEW_WITHOUT_BOOKING.getMessage());
        }
        var comment = commentRepository.save(commentMapper.toModel(commentCreateDto, author, item, dateTime));
        return commentMapper.toDto(comment);
    }

    private BookingShort getNextBooking(BookingShort next, BookingShort current) {
        if (next == null) return current;
        if (current == null) return next;
        if (current.getStart().isBefore(next.getStart())) {
            return current;
        }
        return next;
    }
}