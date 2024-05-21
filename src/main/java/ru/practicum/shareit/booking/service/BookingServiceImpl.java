package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto create(long userId, BookingCreateDto bookingCreateDto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(
                        bookingCreateDto.getItemId())));
        if (!item.getAvailable()) {
                throw new AccessDeniedException(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(item.getId()));
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException(ErrorMessages.BOOKER_CANNOT_BE_OWNER.getMessage());
        }
        var booking = bookingMapper.dtoToBooking(bookingCreateDto, user, item);
        return bookingMapper.bookingToDtoResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findById(long id, Long userId) {
        var booking = bookingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(id)));
        return bookingMapper.bookingToDtoResponse(booking);
    }

    @Override
    @Transactional
    public BookingDto updateStatus(long id, Long ownerId, boolean approved) {
        var booking = bookingRepository.findByIdAndItemOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(id)));
        if (BookingStatus.APPROVED.equals(booking.getStatus())) {
            throw new AccessDeniedException(ErrorMessages.STATUS_APPROVED.getMessage());
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);
        return bookingMapper.bookingToDtoResponse(booking);
    }

    @Override
    public List<BookingDto> findAllForUser(Long bookerId, BookingState state, int from, Integer size) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(bookerId)));
        var sort = Sort.by("start").descending();
        List<Booking> result = Collections.emptyList();
        if (size != null) {
            Pageable pageable = PageRequest.of(from / size, size, sort);
            switch (state) {
                case ALL:
                    result = bookingRepository.findAllByBookerId(bookerId, pageable);
                    break;
                case CURRENT:
                    result = bookingRepository.findAllByBookerCurrent(bookerId, LocalDateTime.now(), pageable);
                    break;
                case REJECTED:
                    result = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, pageable);
                    break;
                case WAITING:
                    result = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.WAITING, pageable);
                    break;
                case FUTURE:
                    result = bookingRepository.findAllByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), pageable);
                    break;
                case PAST:
                    result = bookingRepository.findAllByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), pageable);
                    break;
            }
            return bookingMapper.bookingsToDtoResponse(result);
        }
        switch (state) {
            case ALL:
                result = bookingRepository.findAllByBookerId(bookerId, sort);
                break;
            case CURRENT:
                result = bookingRepository.findAllByBookerCurrent(bookerId, LocalDateTime.now(), sort);
                break;
            case REJECTED:
                result = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, sort);
                break;
            case WAITING:
                result = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.WAITING, sort);
                break;
            case FUTURE:
                result = bookingRepository.findAllByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), sort);
                break;
            case PAST:
                result = bookingRepository.findAllByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), sort);
                break;
        }
        return bookingMapper.bookingsToDtoResponse(result);
    }

    @Override
    public List<BookingDto> findAllForOwner(Long ownerId, BookingState state, int from, Integer size) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(ownerId)));

        List<Booking> result = Collections.emptyList();
        var sort = Sort.by("start").descending();
        if (size != null) {
            Pageable pageable = PageRequest.of(from / size, size, sort);
            switch (state) {
                case ALL:
                    result = bookingRepository.findAllByItemOwnerId(ownerId, pageable);
                    break;
                case CURRENT:
                    result = bookingRepository.findAllByItemOwnerCurrent(ownerId, LocalDateTime.now(), pageable);
                    break;
                case REJECTED:
                    result = bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED,
                            pageable);
                    break;
                case WAITING:
                    result = bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageable);
                    break;
                case FUTURE:
                    result = bookingRepository.findAllByItemOwnerIdAndStartAfter(ownerId, LocalDateTime.now(),
                            pageable);
                    break;
                case PAST:
                    result = bookingRepository.findAllByItemOwnerIdAndEndBefore(ownerId, LocalDateTime.now(),
                            pageable);
                    break;
            }
            return bookingMapper.bookingsToDtoResponse(result);
        }
        switch (state) {
            case ALL:
                result = bookingRepository.findAllByItemOwnerId(ownerId, sort);
                break;
            case CURRENT:
                result = bookingRepository.findAllByItemOwnerCurrent(ownerId, LocalDateTime.now(), sort);
                break;
            case REJECTED:
                result = bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, sort);
                break;
            case WAITING:
                result = bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, sort);
                break;
            case FUTURE:
                result = bookingRepository.findAllByItemOwnerIdAndStartAfter(ownerId, LocalDateTime.now(), sort);
                break;
            case PAST:
                result = bookingRepository.findAllByItemOwnerIdAndEndBefore(ownerId, LocalDateTime.now(), sort);
                break;
        }
        return bookingMapper.bookingsToDtoResponse(result);
    }
}