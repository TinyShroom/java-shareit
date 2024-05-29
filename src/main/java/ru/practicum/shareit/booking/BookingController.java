package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.logging.Logging;
import ru.practicum.shareit.util.PageRequestWithOffset;

import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String DEFAULT_BOOKING_STATE = "ALL";

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @Logging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(@RequestHeader(HEADER_USER_ID) long userId,
                             @Validated
                             @RequestBody
                             BookingCreateDto bookingCreateDto) {
        return bookingService.create(userId, bookingCreateDto);
    }

    @Logging
    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(HEADER_USER_ID) long ownerId,
                              @PathVariable long bookingId,
                              @RequestParam boolean approved) {
        return bookingService.updateStatus(bookingId, ownerId, approved);
    }

    @Logging
    @GetMapping("/{bookingId}")
    public BookingDto get(@RequestHeader(HEADER_USER_ID) long userId,
                          @PathVariable long bookingId) {
        return bookingService.findById(bookingId, userId);
    }

    @Logging
    @GetMapping
    public List<BookingDto> getAllForUser(@RequestHeader(HEADER_USER_ID) long bookerId,
                                          @RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state,
                                          @RequestParam(defaultValue = "0") @Min(0) int from,
                                          @RequestParam(defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        return bookingService.findAllForUser(bookerId, BookingState.parse(state)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.UNKNOWN_STATE.getFormatMessage(state))),
                pageable);
    }

    @Logging
    @GetMapping("/owner")
    public List<BookingDto> getAllForOwner(@RequestHeader(HEADER_USER_ID) long ownerId,
                                           @RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state,
                                           @RequestParam(defaultValue = "0") @Min(0) int from,
                                           @RequestParam(defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        return bookingService.findAllForOwner(ownerId, BookingState.parse(state)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.UNKNOWN_STATE.getFormatMessage(state))),
                pageable);
    }
}
