package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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

    private ItemDto itemCreateDto;

    private final EntityManager em;

    @BeforeEach
    public void setUp() {
        bookerDto = UserDto.builder()
                .name("username")
                .email("user@mail.com")
                .build();

        ownerDto = UserDto.builder()
                .name("ownername")
                .email("owner@mail.com")
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
}