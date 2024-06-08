package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.util.PageRequestWithOffset;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final EntityManager em;

    private final long ownerId = 1;
    private final long bookerId = 2;
    private final long userId = 3;
    private final long unknownUserId = 99999;

    private final long itemIdFirst = 1;
    private final long unknownItemId = 99999;

    private final long unknownBookingId = 99999;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createOk() {
        var start = LocalDateTime.now().plusMonths(1);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemIdFirst)
                .start(start)
                .end(start.plusDays(1))
                .build();
        var bookingDto = bookingService.create(bookerId, bookingCreateDto);
        var query = em.createQuery("select b from Booking b where b.id = :id", Booking.class);
        var result = query.setParameter("id", bookingDto.getId()).getSingleResult();

        assertThat(result.getId(), equalTo(bookingDto.getId()));
        assertThat(result.getStatus(), equalTo(bookingDto.getStatus()));
        assertThat(result.getBooker().getId(), equalTo(bookerId));
        assertThat(result.getItem().getId(), equalTo(itemIdFirst));
        assertThat(result.getStart(), equalTo(start));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createUnknownBookerFail() {
        var start = LocalDateTime.now().plusMonths(1);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemIdFirst)
                .start(start)
                .end(start.plusDays(1))
                .build();
        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(unknownUserId, bookingCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createUnknownItemFail() {
        var start = LocalDateTime.now().plusMonths(1);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(unknownItemId)
                .start(start)
                .end(start.plusDays(1))
                .build();
        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(bookerId, bookingCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(unknownItemId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createBookerIsOwnerFail() {
        var start = LocalDateTime.now().plusMonths(1);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemIdFirst)
                .start(start)
                .end(start.plusDays(1))
                .build();
        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(ownerId, bookingCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.BOOKER_CANNOT_BE_OWNER.getMessage()));
    }

    @Test
    public void findByIdOk() {
        var result = em.createQuery("select b from Booking b", Booking.class)
                .getResultStream()
                .findAny()
                .get();
        var bookings = bookingService.findById(result.getId(), bookerId);
        assertThat(bookings.getId(), equalTo(result.getId()));
        assertThat(bookings.getStatus(), equalTo(result.getStatus()));
        assertThat(bookings.getBooker().getId(), equalTo(result.getBooker().getId()));
        assertThat(bookings.getBooker().getId(), equalTo(bookerId));
        assertThat(bookings.getItem().getId(), equalTo(result.getItem().getId()));
        assertThat(bookings.getStart(), equalTo(result.getStart()));
        assertThat(bookings.getEnd(), equalTo(result.getEnd()));

        bookings = bookingService.findById(result.getId(), ownerId);
        assertThat(bookings.getId(), equalTo(result.getId()));
        assertThat(bookings.getStatus(), equalTo(result.getStatus()));
        assertThat(bookings.getBooker().getId(), equalTo(result.getBooker().getId()));
        assertThat(bookings.getItem().getId(), equalTo(result.getItem().getId()));
        assertThat(bookings.getStart(), equalTo(result.getStart()));
        assertThat(bookings.getEnd(), equalTo(result.getEnd()));
    }

    @Test
    public void findByIdUnknownUserFail() {
        var bookingId = em.createQuery("select b from Booking b", Booking.class)
                .getResultStream()
                .findAny()
                .get().getId();

        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.findById(bookingId, userId));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(bookingId)));

        exception = assertThrows(NotFoundException.class,
                () -> bookingService.findById(unknownBookingId, ownerId));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(unknownBookingId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateStatusOk() {
        var query = em.createQuery("select b from Booking b where b.status = :id", Booking.class);
        var result = query.setParameter("id", BookingStatus.WAITING)
                .getResultList()
                .stream()
                .findAny()
                .get();
        var updatedBooking = bookingService.updateStatus(result.getId(), ownerId, false);
        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.REJECTED));

        updatedBooking = bookingService.updateStatus(result.getId(), ownerId, true);
        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateStatusNotFoundFail() {
        var query = em.createQuery("select b from Booking b where b.status = :id", Booking.class);
        var result = query.setParameter("id", BookingStatus.WAITING)
                .getResultList()
                .stream()
                .findAny()
                .get();
        var exception = assertThrows(NotFoundException.class,
                () -> bookingService.updateStatus(result.getId(), bookerId, false));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(result.getId())));
        exception = assertThrows(NotFoundException.class,
                () -> bookingService.updateStatus(unknownBookingId, ownerId, false));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.BOOKING_NOT_FOUND.getFormatMessage(unknownBookingId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateStatusAlreadyApprovedFail() {
        var query = em.createQuery("select b from Booking b where b.status = :id", Booking.class);
        var result = query.setParameter("id", BookingStatus.WAITING)
                .getResultList()
                .stream()
                .findAny()
                .get();
        bookingService.updateStatus(result.getId(), ownerId, true);
        var exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.updateStatus(result.getId(), ownerId, false));
        assertThat(exception.getMessage(),
                equalTo(ErrorMessages.STATUS_APPROVED.getMessage()));
    }

    @Test
    public void findAllForUserAllOk() {
        var bookings = getBookingsForUser();

        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForUser(bookerId, BookingState.ALL, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 2;
        size = 2;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForUser(bookerId, BookingState.ALL, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForUserCurrentOk() {
        var bookings = getBookingsForUser();

        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getStart().isBefore(getCurrentTime()) && b.getEnd().isAfter(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForUser(bookerId, BookingState.CURRENT, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 0;
        size = 1;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForUser(bookerId, BookingState.CURRENT, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForUserRejectedOk() {
        var bookings = getBookingsForUser();

        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForUser(bookerId, BookingState.REJECTED, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 2;
        size = 2;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForUser(bookerId, BookingState.REJECTED, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForUserWaitingOk() {
        var bookings = getBookingsForUser();

        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForUser(bookerId, BookingState.WAITING, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 2;
        size = 2;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForUser(bookerId, BookingState.WAITING, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForUserFutureOk() {
        var bookings = getBookingsForUser();

        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getStart().isAfter(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForUser(bookerId, BookingState.FUTURE, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 0;
        size = 1;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForUser(bookerId, BookingState.FUTURE, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForUserPastOk() {
        var bookings = getBookingsForUser();

        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getEnd().isBefore(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForUser(bookerId, BookingState.PAST, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 0;
        size = 1;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForUser(bookerId, BookingState.PAST, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForOwnerAllOk() {
        var bookings = getBookingsForOwner();
        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForOwner(ownerId, BookingState.ALL, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 2;
        size = 2;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForOwner(ownerId, BookingState.ALL, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForOwnerCurrentOk() {
        var bookings = getBookingsForOwner();
        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getStart().isBefore(getCurrentTime()) && b.getEnd().isAfter(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForOwner(ownerId, BookingState.CURRENT, pageable);
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 0;
        size = 1;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForOwner(ownerId, BookingState.CURRENT, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForOwnerRejectOk() {
        var bookings = getBookingsForOwner();
        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForOwner(ownerId, BookingState.REJECTED, pageable);
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 2;
        size = 2;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForOwner(ownerId, BookingState.REJECTED, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForOwnerWaitingOk() {
        var bookings = getBookingsForOwner();
        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForOwner(ownerId, BookingState.WAITING, pageable);
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 2;
        size = 2;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForOwner(ownerId, BookingState.WAITING, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForOwnerFutureOk() {
        var bookings = getBookingsForOwner();
        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getStart().isAfter(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForOwner(ownerId, BookingState.FUTURE, pageable);
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 0;
        size = 1;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForOwner(ownerId, BookingState.FUTURE, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    @Test
    public void findAllForOwnerPastOk() {
        var bookings = getBookingsForOwner();
        var from = 0;
        var size = 100;
        var bookingToCompare = bookings.stream()
                .filter(b -> b.getEnd().isBefore(getCurrentTime()))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var result = bookingService.findAllForOwner(ownerId, BookingState.PAST, pageable);
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);

        from = 0;
        size = 1;
        bookingToCompare = bookingToCompare.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        result = bookingService.findAllForOwner(ownerId, BookingState.PAST, pageable);
        assertThat(result, hasSize(bookingToCompare.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(bookingToCompare);
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    private List<BookingDto> getBookingsForUser() {
        var query = em.createQuery("select b from Booking b where b.booker.id = :id", Booking.class);
        return query.setParameter("id", bookerId).getResultList().stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private List<BookingDto> getBookingsForOwner() {
        var query = em.createQuery("select b from Booking b where b.item.owner.id = :id", Booking.class);
        return query.setParameter("id", ownerId).getResultList().stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }
}