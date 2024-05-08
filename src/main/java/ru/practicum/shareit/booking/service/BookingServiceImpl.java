package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnknownStateException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDtoResponse create(BookingDtoRequest bookingDtoRequest) {
        var user = userRepository.findById(bookingDtoRequest.getBookerId())
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found",
                        bookingDtoRequest.getBookerId())));
        var item = itemRepository.findById(bookingDtoRequest.getItemId())
                .orElseThrow(() -> new NotFoundException(String.format("item with id == %d not found",
                        bookingDtoRequest.getItemId())));
        if (!item.getAvailable()) {
                throw new AccessDeniedException(String.format("item with id == %d not available", item.getId()));
        }
        if (item.getOwner().getId().equals(bookingDtoRequest.getBookerId())) {
            throw new NotFoundException("booker cannot be a owner");
        }
        var booking = bookingMapper.dtoToBooking(bookingDtoRequest, user, item);
        return bookingMapper.bookingToDtoResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoResponse findById(long id, Long userId) {
        var booking = bookingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(String.format("booking with id == %d not found", id)));
        return bookingMapper.bookingToDtoResponse(booking);
    }

    @Override
    public BookingDtoResponse updateStatus(long id, Long ownerId, boolean approved) {
        var booking = bookingRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException(String.format("booking with id == %d not found", id)));
        if (BookingStatus.APPROVED.equals(booking.getStatus())) {
            throw new AccessDeniedException("Status already approved");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);
        return bookingMapper.bookingToDtoResponse(booking);
    }

    @Override
    public List<BookingDtoResponse> findAllForUser(Long bookerId, String state) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (Exception e) {
            throw new UnknownStateException("Unknown state: " + state);
        }
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", bookerId)));

        List<Booking> result = Collections.emptyList();
        switch (bookingState) {
            case ALL:
                result = bookingRepository.findAllByBooker(bookerId);
                break;
            case CURRENT:
                result = bookingRepository.findAllByBookerCurrent(bookerId, LocalDateTime.now());
                break;
            case REJECTED:
                result = bookingRepository.findAllByBookerAndStatus(bookerId, BookingStatus.REJECTED);
                break;
            case WAITING:
                result = bookingRepository.findAllByBookerAndStatus(bookerId, BookingStatus.WAITING);
                break;
            case FUTURE:
                result = bookingRepository.findAllByBookerFuture(bookerId, LocalDateTime.now());
                break;
            case PAST:
                result = bookingRepository.findAllByBookerPast(bookerId, LocalDateTime.now());
                break;
        }
        return bookingMapper.bookingsToDtoResponse(result);
    }

    @Override
    public List<BookingDtoResponse> findAllForOwner(Long ownerId, String state) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (Exception e) {
            throw new UnknownStateException("Unknown state: " + state);
        }
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", ownerId)));

        List<Booking> result = Collections.emptyList();
        switch (bookingState) {
            case ALL:
                result = bookingRepository.findAllByOwner(ownerId);
                break;
            case CURRENT:
                result = bookingRepository.findAllByOwnerCurrent(ownerId, LocalDateTime.now());
                break;
            case REJECTED:
                result = bookingRepository.findAllByOwnerAndStatus(ownerId, BookingStatus.REJECTED);
                break;
            case WAITING:
                result = bookingRepository.findAllByOwnerAndStatus(ownerId, BookingStatus.WAITING);
                break;
            case FUTURE:
                result = bookingRepository.findAllByOwnerFuture(ownerId, LocalDateTime.now());
                break;
            case PAST:
                result = bookingRepository.findAllByOwnerPast(ownerId, LocalDateTime.now());
                break;
        }
        return bookingMapper.bookingsToDtoResponse(result);
    }
}