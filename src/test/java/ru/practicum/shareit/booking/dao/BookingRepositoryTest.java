package ru.practicum.shareit.booking.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {"db.url=jdbc:h2:mem:test;MODE=PostgreSQL", "db.driver=org.h2.Driver"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRepositoryTest {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    private User user;
    private User owner;
    private User booker;
    private List<Item> items;
    private List<Booking> bookings;

    @BeforeEach
    public void setUp() {
        var ownerCreate = new User();
        ownerCreate.setName("owner name");
        ownerCreate.setEmail("owner@mail.com");
        owner = userRepository.save(ownerCreate);
        var bookerCreate = new User();
        bookerCreate.setName("booker name");
        bookerCreate.setEmail("booker@mail.com");
        booker = userRepository.save(bookerCreate);
        var userCreate = new User();
        userCreate.setName("user name");
        userCreate.setEmail("user@mail.com");
        user = userRepository.save(userCreate);

        items = new ArrayList<>();
        bookings = new ArrayList<>();
        var currentTime = getCurrentTime().minusDays(1);
        for (var i = 0; i < 6; ++i) {
            var start = currentTime.plusDays(i / 2);

            var item = new Item();
            item.setName("item " + i);
            item.setDescription("item description " + i);
            item.setAvailable(true);
            item.setOwner(owner);
            var savedItem = itemRepository.save(item);
            items.add(savedItem);

            var booking = new Booking();
            booking.setItem(savedItem);
            booking.setBooker(booker);
            booking.setStatus(BookingStatus.APPROVED);
            booking.setStart(start);
            booking.setEnd(start.plusHours(1));
            bookings.add(bookingRepository.save(booking));
        }
    }

    @Test
    void findByIdAndUserIdOk() {
        var booking = bookings.get(0);
        var optional = bookingRepository.findByIdAndUserId(booking.getId(), booking.getBooker().getId());
        assertTrue(optional.isPresent());
        var result = optional.get();
        assertThat(result.getId(), equalTo(booking.getId()));
        assertThat(result.getStatus(), equalTo(booking.getStatus()));
        assertThat(result.getBooker().getId(), equalTo(booking.getBooker().getId()));
        assertThat(result.getStart(), equalTo(booking.getStart()));
        assertThat(result.getEnd(), equalTo(booking.getEnd()));
        assertThat(result.getItem().getId(), equalTo(booking.getItem().getId()));

        optional = bookingRepository.findByIdAndUserId(booking.getId(), booking.getItem().getOwner().getId());
        assertTrue(optional.isPresent());
        result = optional.get();
        assertThat(result.getId(), equalTo(booking.getId()));
        assertThat(result.getStatus(), equalTo(booking.getStatus()));
        assertThat(result.getBooker().getId(), equalTo(booking.getBooker().getId()));
        assertThat(result.getStart(), equalTo(booking.getStart()));
        assertThat(result.getEnd(), equalTo(booking.getEnd()));
        assertThat(result.getItem().getId(), equalTo(booking.getItem().getId()));
    }

    @Test
    void findByIdAndUserIdNotFound() {
        var booking = bookings.get(0);
        var optional = bookingRepository.findByIdAndUserId(booking.getId(), user.getId());
        assertTrue(optional.isEmpty());
    }

    @Test
    void findAllByBookerCurrentOk() {
        var currentTime = getCurrentTime();
        var booking = bookings.stream()
                .filter(b -> b.getBooker().getId().equals(booker.getId()))
                .filter(b -> b.getStart().isBefore(currentTime) && b.getEnd().isAfter(currentTime))
                .collect(Collectors.toList());
        var sort = Sort.by("start").descending();
        var result = bookingRepository.findAllByBookerCurrent(booker.getId(), currentTime, sort);
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(booking.get(0).getId()));
        assertThat(result.get(1).getId(), equalTo(booking.get(1).getId()));
    }

    @Test
    void findAllByBookerCurrentPageableOk() {
        var sort = Sort.by("start").descending();
        Pageable pageable = PageRequest.of(0, 1, sort);

        var currentTime = getCurrentTime();
        var booking = bookings.stream()
                .filter(b -> b.getBooker().getId().equals(booker.getId()))
                .filter(b -> b.getStart().isBefore(currentTime) && b.getEnd().isAfter(currentTime))
                .collect(Collectors.toList());
        var result = bookingRepository.findAllByBookerCurrent(booker.getId(), LocalDateTime.now(), pageable);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(booking.get(0).getId()));
    }

    @Test
    void findAllByOwnerCurrentOk() {
        var currentTime = getCurrentTime();
        var booking = bookings.stream()
                .filter(b -> b.getItem().getOwner().getId().equals(owner.getId()))
                .filter(b -> b.getStart().isBefore(currentTime) && b.getEnd().isAfter(currentTime))
                .collect(Collectors.toList());
        var sort = Sort.by("start").descending();
        var result = bookingRepository.findAllByItemOwnerCurrent(owner.getId(), currentTime, sort);
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(booking.get(0).getId()));
        assertThat(result.get(1).getId(), equalTo(booking.get(1).getId()));
    }

    @Test
    void findAllByOwnerCurrentPageableOk() {
        var sort = Sort.by("start").descending();
        Pageable pageable = PageRequest.of(0, 1, sort);

        var currentTime = getCurrentTime();
        var booking = bookings.stream()
                .filter(b -> b.getItem().getOwner().getId().equals(owner.getId()))
                .filter(b -> b.getStart().isBefore(currentTime) && b.getEnd().isAfter(currentTime))
                .collect(Collectors.toList());
        var result = bookingRepository.findAllByItemOwnerCurrent(owner.getId(), LocalDateTime.now(), pageable);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(booking.get(0).getId()));
    }

    @Test
    void findAllBookingsShortByItemIdInOk() {
        var sort = Sort.by("start").descending();
        Pageable pageable = PageRequest.of(0, 1, sort);
        var itemsId = items.stream()
                .limit(items.size() / 2)
                .map(Item::getId)
                .collect(Collectors.toList());
        var bookingsHalf = bookings.stream()
                .filter(b -> itemsId.contains(b.getItem().getId()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        var result = bookingRepository.findAllBookingsShortByItemIdIn(itemsId, sort);

        assertThat(result, hasSize(itemsId.size()));
        assertThat(result.get(0).getId(), equalTo(bookingsHalf.get(0).getId()));
        assertThat(result.get(1).getId(), equalTo(bookingsHalf.get(1).getId()));
        assertThat(result.get(2).getId(), equalTo(bookingsHalf.get(2).getId()));
    }

    @Test
    void findBookingsShortByItemOk() {
        var result = bookingRepository.findBookingsShortByItem(items.get(0).getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(bookings.get(0).getId()));
        assertThat(result.get(0).getItemId(), equalTo(bookings.get(0).getItem().getId()));
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }
}