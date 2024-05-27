package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = {"db.url=jdbc:h2:mem:test;MODE=PostgreSQL", "db.driver=org.h2.Driver"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    private UserDto bookerDto;

    private UserDto ownerDto;

    private UserDto userDto;

    private ItemDto itemCreateDto;

    private final EntityManager em;

    @BeforeEach
    public void setUp() {
        bookerDto = UserDto.builder()
                .name("bookername")
                .email("booker@mail.com")
                .build();

        ownerDto = UserDto.builder()
                .name("ownername")
                .email("owner@mail.com")
                .build();

        userDto = UserDto.builder()
                .name("username")
                .email("user@mail.com")
                .build();

        itemCreateDto = ItemDto.builder()
                .name("item")
                .description("item_description")
                .available(true)
                .build();
    }

    @Test
    public void createOk() {
        var start = LocalDateTime.now().plusHours(1);
        var owner = userService.create(ownerDto);
        var booker = userService.create(bookerDto);
        var item = itemService.create(owner.getId(), itemCreateDto);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();
        var bookingDto = bookingService.create(booker.getId(), bookingCreateDto);
        var query = em.createQuery("select b from Booking b where b.id = :id", Booking.class);
        var request = query.setParameter("id", bookingDto.getId()).getSingleResult();

        assertThat(request.getId(), equalTo(bookingDto.getId()));
        assertThat(request.getStatus(), equalTo(bookingDto.getStatus()));
        assertThat(request.getBooker().getId(), equalTo(booker.getId()));
        assertThat(request.getItem().getId(), equalTo(item.getId()));
        assertThat(request.getStart(), equalTo(start));
        assertThat(request.getEnd(), equalTo(bookingDto.getEnd()));
    }

    @Test
    public void createUnknownBookerFail() {
        var start = LocalDateTime.now().plusHours(1);
        var owner = userService.create(ownerDto);
        var unknownBookerId = owner.getId() + 1;
        var item = itemService.create(owner.getId(), itemCreateDto);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();
        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(unknownBookerId, bookingCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownBookerId)));
    }

    @Test
    public void createUnknownItemFail() {
        var start = LocalDateTime.now().plusHours(1);
        var booker = userService.create(bookerDto);
        var unknownItemId = 1L;
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(unknownItemId)
                .start(start)
                .end(start.plusDays(1))
                .build();
        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(booker.getId(), bookingCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(unknownItemId)));
    }

    @Test
    public void createBookerIsOwnerFail() {
        var start = LocalDateTime.now().plusHours(1);
        var owner = userService.create(ownerDto);
        var item = itemService.create(owner.getId(), itemCreateDto);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();
        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(owner.getId(), bookingCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.BOOKER_CANNOT_BE_OWNER.getMessage()));
    }

    @Test
    public void findByIdOk() {
        var start = LocalDateTime.now().plusHours(1);
        var owner = userService.create(ownerDto);
        var booker = userService.create(bookerDto);
        var item = itemService.create(owner.getId(), itemCreateDto);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();
        var bookingDto = bookingService.create(booker.getId(), bookingCreateDto);
        var result = bookingService.findById(bookingDto.getId(), booker.getId());
        assertThat(result.getId(), equalTo(bookingDto.getId()));
        assertThat(result.getStatus(), equalTo(bookingDto.getStatus()));
        assertThat(result.getBooker().getId(), equalTo(booker.getId()));
        assertThat(result.getItem().getId(), equalTo(item.getId()));
        assertThat(result.getStart(), equalTo(start));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));

        result = bookingService.findById(bookingDto.getId(), owner.getId());
        assertThat(result.getId(), equalTo(bookingDto.getId()));
        assertThat(result.getStatus(), equalTo(bookingDto.getStatus()));
        assertThat(result.getBooker().getId(), equalTo(booker.getId()));
        assertThat(result.getItem().getId(), equalTo(item.getId()));
        assertThat(result.getStart(), equalTo(start));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
    }

    @Test
    public void findByIdUnknownUserFail() {
        var start = LocalDateTime.now().plusHours(1);
        var owner = userService.create(ownerDto);
        var booker = userService.create(bookerDto);
        var user = userService.create(userDto);
        var item = itemService.create(owner.getId(), itemCreateDto);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();
        var bookingDto = bookingService.create(booker.getId(), bookingCreateDto);
        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.findById(bookingDto.getId(), user.getId()));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(bookingDto.getId())));

        var unknownBookingId = bookingDto.getId() + 1;
        exception = assertThrows(NotFoundException.class,
                () -> bookingService.findById(unknownBookingId, owner.getId()));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(unknownBookingId)));
    }

    @Test
    public void updateStatusOk() {
        var start = LocalDateTime.now().plusHours(1);
        var owner = userService.create(ownerDto);
        var booker = userService.create(bookerDto);
        var item = itemService.create(owner.getId(), itemCreateDto);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();
        var bookingDto = bookingService.create(booker.getId(), bookingCreateDto);
        var updatedBooking = bookingService.updateStatus(bookingDto.getId(), owner.getId(), false);
        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.REJECTED));

        updatedBooking = bookingService.updateStatus(bookingDto.getId(), owner.getId(), true);
        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    public void updateStatusNotFoundFail() {
        var start = LocalDateTime.now().plusHours(1);
        var owner = userService.create(ownerDto);
        var booker = userService.create(bookerDto);
        var item = itemService.create(owner.getId(), itemCreateDto);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();
        var bookingDto = bookingService.create(booker.getId(), bookingCreateDto);
        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.updateStatus(bookingDto.getId(), booker.getId(), false));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(bookingDto.getId())));
        var unknownBookingId = bookingDto.getId() + 1;
        exception = assertThrows(NotFoundException.class,
                () -> bookingService.updateStatus(unknownBookingId, owner.getId(), false));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(unknownBookingId)));
    }

    @Test
    public void updateStatusAlreadyApprovedFail() {
        var start = LocalDateTime.now().plusHours(1);
        var owner = userService.create(ownerDto);
        var booker = userService.create(bookerDto);
        var item = itemService.create(owner.getId(), itemCreateDto);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(start.plusDays(1))
                .build();
        var bookingDto = bookingService.create(booker.getId(), bookingCreateDto);
        bookingService.updateStatus(bookingDto.getId(), owner.getId(), true);
        var exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.updateStatus(bookingDto.getId(), owner.getId(), false));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.STATUS_APPROVED.getMessage()));
    }

    @Test
    public void findAllForUserOk() {
        var owner = userService.create(ownerDto);
        var booker = userService.create(bookerDto);
        List<ItemDto> items = new ArrayList<>();
        for (var i = 0; i < 2; ++i) {
            items.add(itemService.create(owner.getId(), itemCreateDto));
        }
        var currentDate = getCurrentTime();
        var past = currentDate.minusDays(1).minusMinutes(30);
        List<BookingDto> bookings = new ArrayList<>();
        for (var i = 0; i < 3; ++i) {
            var start = past.plusDays(i);
            for (var j = 0; j < items.size(); ++j) {
                var bookingCreateDto = BookingCreateDto.builder()
                        .itemId(items.get(j).getId())
                        .start(start.plusSeconds(j))
                        .end(start.plusHours(1).plusSeconds(j))
                        .build();
                var booking = bookingService.create(booker.getId(), bookingCreateDto);
                if (j % 2 == 0) {
                    bookings.add(booking);
                } else {
                    bookings.add(bookingService.updateStatus(booking.getId(), owner.getId(), false));
                }
            }
        }
        var bookingToCompare = bookings.stream()
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        var result = bookingService.findAllForUser(booker.getId(), BookingState.ALL, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        var resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        var from = 2;
        var size = 2;
        result = bookingService.findAllForUser(booker.getId(), BookingState.ALL, from, size);
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));
        assertThat(result.get(1).getId(), equalTo(bookingToCompare.get((from / size) * size + 1).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getStart().isBefore(getCurrentTime()) && b.getEnd().isAfter(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForUser(booker.getId(), BookingState.CURRENT, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        from = 0;
        size = 1;
        result = bookingService.findAllForUser(booker.getId(), BookingState.CURRENT, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForUser(booker.getId(), BookingState.REJECTED, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        from = 2;
        size = 2;
        result = bookingService.findAllForUser(booker.getId(), BookingState.REJECTED, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForUser(booker.getId(), BookingState.WAITING, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        result = bookingService.findAllForUser(booker.getId(), BookingState.WAITING, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getStart().isAfter(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForUser(booker.getId(), BookingState.FUTURE, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        from = 0;
        size = 1;
        result = bookingService.findAllForUser(booker.getId(), BookingState.FUTURE, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getEnd().isBefore(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForUser(booker.getId(), BookingState.PAST, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        result = bookingService.findAllForUser(booker.getId(), BookingState.PAST, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));
    }

    @Test
    public void findAllForOwnerOk() {
        var owner = userService.create(ownerDto);
        var booker = userService.create(bookerDto);
        List<ItemDto> items = new ArrayList<>();
        for (var i = 0; i < 2; ++i) {
            items.add(itemService.create(owner.getId(), itemCreateDto));
        }
        var currentDate = getCurrentTime();
        var past = currentDate.minusDays(1).minusMinutes(30);
        List<BookingDto> bookings = new ArrayList<>();
        for (var i = 0; i < 3; ++i) {
            var start = past.plusDays(i);
            for (var j = 0; j < items.size(); ++j) {
                var bookingCreateDto = BookingCreateDto.builder()
                        .itemId(items.get(j).getId())
                        .start(start.plusSeconds(j))
                        .end(start.plusHours(1).plusSeconds(j))
                        .build();
                var booking = bookingService.create(booker.getId(), bookingCreateDto);
                if (j % 2 == 0) {
                    bookings.add(booking);
                } else {
                    bookings.add(bookingService.updateStatus(booking.getId(), owner.getId(), false));
                }
            }
        }
        var bookingToCompare = bookings.stream()
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        var result = bookingService.findAllForOwner(owner.getId(), BookingState.ALL, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        var resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        var from = 2;
        var size = 2;
        result = bookingService.findAllForOwner(owner.getId(), BookingState.ALL, from, size);
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));
        assertThat(result.get(1).getId(), equalTo(bookingToCompare.get((from / size) * size + 1).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getStart().isBefore(getCurrentTime()) && b.getEnd().isAfter(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForOwner(owner.getId(), BookingState.CURRENT, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        from = 0;
        size = 1;
        result = bookingService.findAllForOwner(owner.getId(), BookingState.CURRENT, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForOwner(owner.getId(), BookingState.REJECTED, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        from = 2;
        size = 2;
        result = bookingService.findAllForOwner(owner.getId(), BookingState.REJECTED, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForOwner(owner.getId(), BookingState.WAITING, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        result = bookingService.findAllForOwner(owner.getId(), BookingState.WAITING, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getStart().isAfter(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForOwner(owner.getId(), BookingState.FUTURE, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        from = 0;
        size = 1;
        result = bookingService.findAllForOwner(owner.getId(), BookingState.FUTURE, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));

        bookingToCompare = bookings.stream()
                .filter(b -> b.getEnd().isBefore(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        result = bookingService.findAllForOwner(owner.getId(), BookingState.PAST, 0, null);
        assertThat(result, hasSize(bookingToCompare.size()));
        resultSize = result.size();
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get(0).getId()));
        assertThat(result.get(resultSize - 1).getId(), equalTo(bookingToCompare.get(resultSize - 1).getId()));

        result = bookingService.findAllForOwner(owner.getId(), BookingState.PAST, from, size);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookingToCompare.get((from / size) * size).getId()));
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

}