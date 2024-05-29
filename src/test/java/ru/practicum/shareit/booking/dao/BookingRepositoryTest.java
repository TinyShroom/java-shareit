package ru.practicum.shareit.booking.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingShort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.util.PageRequestWithOffset;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRepositoryTest {

    private final BookingRepository bookingRepository;
    private final EntityManager em;

    private final long ownerId = 1;
    private final long bookerId = 2;
    private final long userId = 3;

    @Test
    void findByIdAndUserIdOk() {
        var booking = em.createQuery("select b from Booking as b", Booking.class)
                .getResultStream()
                .findAny()
                .get();
        var optional = bookingRepository.findByIdAndUserId(booking.getId(), booking.getBooker().getId());
        assertTrue(optional.isPresent());
        var result = optional.get();
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(booking);

        optional = bookingRepository.findByIdAndUserId(booking.getId(), booking.getItem().getOwner().getId());
        assertTrue(optional.isPresent());
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(booking);
    }

    @Test
    void findByIdAndUserIdNotFound() {
        var booking = em.createQuery("select b from Booking as b", Booking.class)
                .getResultStream()
                .findAny()
                .get();
        var optional = bookingRepository.findByIdAndUserId(booking.getId(), userId);
        assertTrue(optional.isEmpty());
    }

    @Test
    void findAllByBookerCurrentOk() {
        var from = 0;
        var size = 10;
        var sort = Sort.by("start").descending();
        Pageable pageable = PageRequestWithOffset.of(from, size, sort);

        var currentTime = getCurrentTime();
        var booking = em.createQuery("select b from Booking as b", Booking.class)
                .getResultStream()
                .filter(b -> b.getBooker().getId().equals(bookerId))
                .filter(b -> b.getStart().isBefore(currentTime) && b.getEnd().isAfter(currentTime))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        var result = bookingRepository.findAllByBookerCurrent(bookerId, LocalDateTime.now(), pageable);

        assertThat(result).hasSize(booking.size());
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(booking);
    }

    @Test
    void findAllByOwnerCurrentOk() {
        var from = 0;
        var size = 10;
        var sort = Sort.by("start").descending();
        Pageable pageable = PageRequestWithOffset.of(from, size, sort);

        var currentTime = getCurrentTime();
        var booking = em.createQuery("select b from Booking as b", Booking.class)
                .getResultStream()
                .filter(b -> b.getItem().getOwner().getId().equals(ownerId))
                .filter(b -> b.getStart().isBefore(currentTime) && b.getEnd().isAfter(currentTime))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        var result = bookingRepository.findAllByItemOwnerCurrent(ownerId, LocalDateTime.now(), pageable);

        assertThat(result).hasSize(booking.size());
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(booking);
    }

    @Test
    void findAllBookingsShortByItemIdInOk() {
        var sort = Sort.by("start").descending();
        var itemsId = em.createQuery("select it from Item as it", Item.class)
                .getResultStream()
                .sorted(Comparator.comparingLong(Item::getId))
                .limit(2)
                .map(Item::getId)
                .collect(Collectors.toList());
        var bookings = em.createQuery("select b from Booking as b where b.item.id in :id", Booking.class)
                .setParameter("id", itemsId)
                .getResultStream()
                .filter(b -> BookingStatus.APPROVED.equals(b.getStatus()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(this::convert)
                .collect(Collectors.toList());
        var result = bookingRepository.findAllBookingsShortByItemIdIn(itemsId, sort);
        assertThat(result).hasSize(bookings.size());
        assertThat(result).usingRecursiveComparison().isEqualTo(bookings);
    }

    @Test
    void findBookingsShortByItemOk() {
        var itemId = em.createQuery("select b from Booking as b", Booking.class)
                .getResultStream()
                .findAny()
                .get().getItem().getId();
        var bookings = em.createQuery("select b from Booking as b where b.item.id = :id", Booking.class)
                .setParameter("id", itemId)
                .getResultStream()
                .filter(b -> BookingStatus.APPROVED.equals(b.getStatus()))
                .map(this::convert)
                .collect(Collectors.toList());
        var result = bookingRepository.findBookingsShortByItem(itemId);

        assertThat(result).hasSize(bookings.size());
        assertThat(result).usingRecursiveComparison().isEqualTo(bookings);
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    private BookingShort convert(Booking booking) {
        if (booking == null) {
            return null;
        }
        var bookerId = booking.getBooker() == null ? null : booking.getBooker().getId();
        var itemId = booking.getItem() == null ? null : booking.getItem().getId();
        var id = booking.getId();
        var start = booking.getStart();
        var end = booking.getEnd();

        return new BookingShort(id, itemId, bookerId, start, end);
    }
}