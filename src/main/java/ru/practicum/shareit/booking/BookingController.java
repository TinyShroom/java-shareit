package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.logging.Logging;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
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
    public BookingDtoResponse create(@RequestHeader(HEADER_USER_ID) Long userId,
                                     @Validated
                                     @RequestBody
                                     BookingDtoRequest bookingDtoRequest) {
        bookingDtoRequest.setBookerId(userId);
        return bookingService.create(bookingDtoRequest);
    }

    @Logging
    @PatchMapping("/{bookingId}")
    public BookingDtoResponse approve(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                      @PathVariable Long bookingId,
                                      @RequestParam boolean approved) {
        return bookingService.updateStatus(bookingId, ownerId, approved);
    }

    @Logging
    @GetMapping("/{bookingId}")
    public BookingDtoResponse get(@RequestHeader(HEADER_USER_ID) Long userId,
                                  @PathVariable Long bookingId) {
        return bookingService.findById(bookingId, userId);
    }

    @Logging
    @GetMapping
    public List<BookingDtoResponse> getAllForUser(@RequestHeader(HEADER_USER_ID) Long bookerId,
                                                  @RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state) {
        return bookingService.findAllForUser(bookerId, state);
    }

    @Logging
    @GetMapping("/owner")
    public List<BookingDtoResponse> getAllForOwner(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                                   @RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state) {
        return bookingService.findAllForOwner(ownerId, state);
    }
}
