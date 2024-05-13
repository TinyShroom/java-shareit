package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.logging.Logging;

import java.util.List;

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
    public BookingDto create(@RequestHeader(HEADER_USER_ID) Long userId,
                             @Validated
                                     @RequestBody
                                     BookingCreateDto bookingCreateDto) {
        bookingCreateDto.setBookerId(userId);
        return bookingService.create(bookingCreateDto);
    }

    @Logging
    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(HEADER_USER_ID) Long ownerId,
                              @PathVariable Long bookingId,
                              @RequestParam boolean approved) {
        return bookingService.updateStatus(bookingId, ownerId, approved);
    }

    @Logging
    @GetMapping("/{bookingId}")
    public BookingDto get(@RequestHeader(HEADER_USER_ID) Long userId,
                          @PathVariable Long bookingId) {
        return bookingService.findById(bookingId, userId);
    }

    @Logging
    @GetMapping
    public List<BookingDto> getAllForUser(@RequestHeader(HEADER_USER_ID) Long bookerId,
                                          @RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state) {
        return bookingService.findAllForUser(bookerId, BookingState.parse(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state)));
    }

    @Logging
    @GetMapping("/owner")
    public List<BookingDto> getAllForOwner(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                           @RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state) {
        return bookingService.findAllForOwner(ownerId, BookingState.parse(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state)));
    }
}
