package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.logging.Logging;

import javax.validation.constraints.Min;
import java.util.Map;


@Validated
@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

	private static final String DEFAULT_BOOKING_STATE = "ALL";

	private static final String HEADER_USER_ID = "X-Sharer-User-Id";

	private final BookingClient bookingClient;

	@Logging
	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader(HEADER_USER_ID) long userId,
										 @Validated
										 @RequestBody
										 BookingCreateDto bookingCreateDto) {
		return bookingClient.create(userId, bookingCreateDto);
	}

	@Logging
	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(@RequestHeader(HEADER_USER_ID) long ownerId,
										  @PathVariable long bookingId,
										  @RequestParam boolean approved) {
		return bookingClient.updateStatus(bookingId, ownerId, approved);
	}

	@Logging
	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> get(@RequestHeader(HEADER_USER_ID) long userId,
									  @PathVariable long bookingId) {
		return bookingClient.getById(userId, bookingId);
	}

	@Logging
	@GetMapping
	public ResponseEntity<Object> getAllForUser(@RequestHeader(HEADER_USER_ID) long userId,
										  		@RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state,
										  		@RequestParam(defaultValue = "0") @Min(0) int from,
										  		@RequestParam(defaultValue = "10") @Min(1) int size) {
		var bookingState = BookingState.parse(state)
				.orElseThrow(() -> new IllegalArgumentException(
						ErrorMessages.UNKNOWN_STATE.getFormatMessage(state)));
		Map<String, Object> parameters = Map.of(
				"state", bookingState,
				"from", from,
				"size", size
		);
		return bookingClient.getAllForUser(userId, parameters);
	}

	@Logging
	@GetMapping("/owner")
	public ResponseEntity<Object> getAllForOwner(@RequestHeader(HEADER_USER_ID) long ownerId,
												 @RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state,
												 @RequestParam(defaultValue = "0") @Min(0) int from,
												 @RequestParam(defaultValue = "10") @Min(1) int size) {
		var bookingState = BookingState.parse(state)
				.orElseThrow(() -> new IllegalArgumentException(
						ErrorMessages.UNKNOWN_STATE.getFormatMessage(state)));
		Map<String, Object> parameters = Map.of(
				"state", bookingState,
				"from", from,
				"size", size
		);
		return bookingClient.getAllForOwner(ownerId, parameters);
	}

}
