package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = {"db.url=jdbc:h2:mem:test;MODE=PostgreSQL", "db.driver=org.h2.Driver"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    private final ItemService itemService;
    private final RequestService requestService;
    private final UserService userService;
    private final BookingService bookingService;
    private final EntityManager em;
    private UserDto userDto;
    private UserDto ownerDto;
    private ItemDto itemCreateDto;
    private RequestCreateDto requestCreateDto;

    @BeforeEach
    public void setUp() {
        userDto = UserDto.builder()
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

        requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
    }

    @Test
    public void createOk() {
        var owner = userService.create(ownerDto);

        var itemDto = itemService.create(owner.getId(), itemCreateDto);

        assertThat(itemDto.getName(), equalTo(itemCreateDto.getName()));
        assertThat(itemDto.getDescription(), equalTo(itemCreateDto.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(itemCreateDto.getAvailable()));
        assertThat(itemDto.getRequestId(), equalTo(itemCreateDto.getRequestId()));

        var query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        var result = query.setParameter("id", itemDto.getId())
                .getSingleResult();

        assertThat(result.getId(), equalTo(itemDto.getId()));
        assertThat(result.getOwner().getId(), equalTo(owner.getId()));
        assertThat(result.getRequest(), nullValue());
        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));

        var user = userService.create(userDto);
        var requestDto = requestService.create(user.getId(), requestCreateDto);
        var itemCreateDtoWithRequest = ItemDto.builder()
                .name("item_rq")
                .description("description_rq")
                .available(true)
                .requestId(requestDto.getId())
                .build();

        var itemDtoRq = itemService.create(owner.getId(), itemCreateDtoWithRequest);
        assertThat(itemDtoRq.getName(), equalTo(itemCreateDtoWithRequest.getName()));
        assertThat(itemDtoRq.getDescription(), equalTo(itemCreateDtoWithRequest.getDescription()));
        assertThat(itemDtoRq.getAvailable(), equalTo(itemCreateDtoWithRequest.getAvailable()));
        assertThat(itemDtoRq.getRequestId(), equalTo(requestDto.getId()));

        result = query.setParameter("id", itemDtoRq.getId())
                .getSingleResult();

        assertThat(result.getId(), equalTo(itemDtoRq.getId()));
        assertThat(result.getOwner().getId(), equalTo(owner.getId()));
        assertThat(result.getRequest().getId(), equalTo(itemDtoRq.getRequestId()));
        assertThat(result.getName(), equalTo(itemDtoRq.getName()));
        assertThat(result.getDescription(), equalTo(itemDtoRq.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDtoRq.getAvailable()));
    }

    @Test
    public void createUnknownOwnerFail() {
        var unknownOwnerId = 1L;

        var exception = assertThrows(NotFoundException.class,
                () -> itemService.create(unknownOwnerId, itemCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownOwnerId)));
    }

    @Test
    public void createUnknownRequestFail() {
        var owner = userService.create(ownerDto);
        var unknownRequestDtoId = 1L;
        itemCreateDto.setRequestId(unknownRequestDtoId);
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.create(owner.getId(), itemCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.REQUEST_NOT_FOUND.getFormatMessage(unknownRequestDtoId)));
    }

    @Test
    public void updateOk() {
        var owner = userService.create(ownerDto);

        var createdItem = itemService.create(owner.getId(), itemCreateDto);

        var updateItemDto = ItemDto.builder()
                .id(createdItem.getId())
                .description("new description")
                .build();

        var itemDto = itemService.update(owner.getId(), updateItemDto);

        assertThat(itemDto.getName(), equalTo(itemCreateDto.getName()));
        assertThat(itemDto.getDescription(), equalTo(updateItemDto.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(itemCreateDto.getAvailable()));
        assertThat(itemDto.getRequestId(), equalTo(itemCreateDto.getRequestId()));
    }

    @Test
    public void updateUnknownUserFail() {
        var owner = userService.create(ownerDto);

        var createdItem = itemService.create(owner.getId(), itemCreateDto);

        var updateItemDto = ItemDto.builder()
                .id(createdItem.getId())
                .description("new description")
                .build();

        var unknownUserId = owner.getId() + 1;
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.update(unknownUserId, updateItemDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    public void updateUnknownItemFail() {
        var owner = userService.create(ownerDto);

        var itemDto = ItemDto.builder()
                .id(1L)
                .build();
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.update(owner.getId(), itemDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(itemDto.getId())));
    }

    @Test
    public void updateNotOwnerFail() {
        var owner = userService.create(ownerDto);
        var user = userService.create(userDto);

        var createdItem = itemService.create(owner.getId(), itemCreateDto);

        var updateItemDto = ItemDto.builder()
                .id(createdItem.getId())
                .description("new description")
                .build();

        var exception = assertThrows(AccessDeniedException.class,
                () -> itemService.update(user.getId(), updateItemDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.OWNER_UPDATE.getFormatMessage(user.getId())));
    }

    @Test
    public void deleteOk() {
        var owner = userService.create(ownerDto);

        var createdItem = itemService.create(owner.getId(), itemCreateDto);
        itemService.delete(owner.getId(), createdItem.getId());
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.findById(owner.getId(), createdItem.getId()));
        assertThat(exception.getMessage(), equalTo(
                ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(createdItem.getId())));
    }

    @Test
    public void deleteUnknownUserFail() {
        var owner = userService.create(ownerDto);
        var createdItem = itemService.create(owner.getId(), itemCreateDto);
        var unknownUserId = owner.getId() + 1;

        var exception = assertThrows(NotFoundException.class,
                () -> itemService.delete(unknownUserId, createdItem.getId()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    public void deleteUnknownItemFail() {
        var owner = userService.create(ownerDto);
        var unknownItemId = 1L;

        var exception = assertThrows(NotFoundException.class,
                () -> itemService.delete(owner.getId(), unknownItemId));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(unknownItemId)));
    }

    @Test
    public void deleteUserNotOwnerFail() {
        var owner = userService.create(ownerDto);
        var user = userService.create(userDto);
        var createdItem = itemService.create(owner.getId(), itemCreateDto);

        var exception = assertThrows(AccessDeniedException.class,
                () -> itemService.delete(user.getId(), createdItem.getId()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.OWNER_DELETE.getFormatMessage(user.getId())));
    }

    @Test
    public void createCommentOk() throws InterruptedException {
        var owner = userService.create(ownerDto);
        var user = userService.create(userDto);

        var item = itemService.create(owner.getId(), itemCreateDto);

        var start = getCurrentTime();
        var end = start.plusSeconds(1);

        var bookingLast = bookingService.create(user.getId(), BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build());
        bookingService.updateStatus(bookingLast.getId(), owner.getId(), true);
        var sleepTime = ChronoUnit.MILLIS.between(LocalDateTime.now(), end) + 1;
        Thread.sleep(sleepTime);
        var comment = itemService.createComment(user.getId(), item.getId(), CommentCreateDto.builder()
                .text("First comment")
                .build());
        var query = em.createQuery("select c from Comment c where c.id = :id", Comment.class);
        var result = query.setParameter("id", comment.getId())
                .getSingleResult();

        assertThat(result.getId(), equalTo(comment.getId()));
        assertThat(result.getAuthor().getName(), equalTo(comment.getAuthorName()));
        assertThat(result.getText(), equalTo(comment.getText()));
        assertThat(result.getCreated(), equalTo(comment.getCreated()));
        assertThat(result.getItem().getId(), equalTo(item.getId()));
        assertThat(result.getAuthor().getId(), equalTo(user.getId()));
    }

    @Test
    public void createCommentWithoutBookingFail() throws InterruptedException {
        var owner = userService.create(ownerDto);
        var user = userService.create(userDto);

        var itemRejected = itemService.create(owner.getId(), itemCreateDto);
        var itemApproved = itemService.create(owner.getId(), itemCreateDto);
        var itemWithoutBooking = itemService.create(owner.getId(), itemCreateDto);

        var start = getCurrentTime().plusSeconds(1);
        var end = start.plusSeconds(1);

        var bookingRejected = bookingService.create(user.getId(), BookingCreateDto.builder()
                .itemId(itemRejected.getId())
                .start(start)
                .end(end)
                .build());
        bookingService.updateStatus(bookingRejected.getId(), owner.getId(), false);
        var bookingAccepted = bookingService.create(user.getId(), BookingCreateDto.builder()
                .itemId(itemApproved.getId())
                .start(start)
                .end(start.plusDays(1))
                .build());
        bookingService.updateStatus(bookingAccepted.getId(), owner.getId(), true);
        var sleepTime = ChronoUnit.MILLIS.between(LocalDateTime.now(), end) + 1;
        Thread.sleep(sleepTime);
        var exception = assertThrows(AccessDeniedException.class,
                () -> itemService.createComment(user.getId(), itemRejected.getId(), CommentCreateDto.builder()
                        .text("First comment")
                        .build()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.REVIEW_WITHOUT_BOOKING.getMessage()));

        exception = assertThrows(AccessDeniedException.class,
                () -> itemService.createComment(user.getId(), itemApproved.getId(), CommentCreateDto.builder()
                        .text("First comment")
                        .build()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.REVIEW_WITHOUT_BOOKING.getMessage()));

        exception = assertThrows(AccessDeniedException.class,
                () -> itemService.createComment(user.getId(), itemWithoutBooking.getId(), CommentCreateDto.builder()
                        .text("First comment")
                        .build()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.REVIEW_WITHOUT_BOOKING.getMessage()));
    }

    @Test
    public void createCommentUnknownItemFail() {
        var user = userService.create(userDto);

        var unknownItemId = 1L;

        var exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(user.getId(), unknownItemId, CommentCreateDto.builder()
                        .text("First comment")
                        .build()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(unknownItemId)));
    }

    @Test
    public void createCommentUnknownUserFail() {
        var owner = userService.create(ownerDto);
        var item = itemService.create(owner.getId(), itemCreateDto);

        var unknownUserId = owner.getId() + 1;

        var exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(unknownUserId, item.getId(), CommentCreateDto.builder()
                        .text("First comment")
                        .build()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    public void findByIdOk() throws InterruptedException {
        var owner = userService.create(ownerDto);
        var user = userService.create(userDto);

        var item = itemService.create(owner.getId(), itemCreateDto);
        var result = itemService.findById(user.getId(), item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.getComments(), hasSize(0));
        assertThat(result.getLastBooking(), nullValue());
        assertThat(result.getNextBooking(), nullValue());

        var start = getCurrentTime().plusSeconds(1);
        var end = start.plusSeconds(1);

        var bookingLast = bookingService.create(user.getId(), BookingCreateDto.builder()
                .itemId(result.getId())
                .start(start)
                .end(end)
                .build());
        bookingService.updateStatus(bookingLast.getId(), owner.getId(), true);
        var sleepTime = ChronoUnit.MILLIS.between(LocalDateTime.now(), end) + 1;
        Thread.sleep(sleepTime);
        itemService.createComment(user.getId(), item.getId(), CommentCreateDto.builder()
                .text("First comment")
                .build());
        itemService.createComment(user.getId(), item.getId(), CommentCreateDto.builder()
                .text("Second comment")
                .build());

        result = itemService.findById(user.getId(), item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.getComments(), hasSize(2));
        assertThat(result.getLastBooking(), nullValue());
        assertThat(result.getNextBooking(), nullValue());

        var booking = bookingService.create(user.getId(), BookingCreateDto.builder()
                .itemId(item.getId())
                .start(getCurrentTime().plusDays(3))
                .end(getCurrentTime().plusDays(4))
                .build());
        var bookingNext = bookingService.create(user.getId(), BookingCreateDto.builder()
                .itemId(item.getId())
                .start(getCurrentTime().plusDays(1))
                .end(getCurrentTime().plusDays(2))
                .build());
        bookingService.updateStatus(booking.getId(), owner.getId(), true);
        bookingService.updateStatus(bookingNext.getId(), owner.getId(), true);

        result = itemService.findById(owner.getId(), item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.getComments(), hasSize(2));
        assertThat(result.getLastBooking().getId(), equalTo(bookingLast.getId()));
        assertThat(result.getLastBooking().getStart(), equalTo(bookingLast.getStart()));
        assertThat(result.getLastBooking().getEnd(), equalTo(bookingLast.getEnd()));
        assertThat(result.getNextBooking().getId(), equalTo(bookingNext.getId()));
        assertThat(result.getNextBooking().getStart(), equalTo(bookingNext.getStart()));
        assertThat(result.getNextBooking().getEnd(), equalTo(bookingNext.getEnd()));
    }

    @Test
    public void findByIdFail() {
        var unknownUserId = 1L;
        var unknownItemId = 1L;
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.findById(unknownUserId, unknownItemId));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(unknownItemId)));
    }

    @Test
    public void findAllOk() throws InterruptedException {
        var owner = userService.create(ownerDto);
        var user = userService.create(userDto);

        var result = itemService.getAll(owner.getId(), 0, null);
        assertThat(result, hasSize(0));

        var usersItem = itemService.create(user.getId(), itemCreateDto);
        var item = itemService.create(owner.getId(), itemCreateDto);
        List<ItemDto> items = new ArrayList<>();
        items.add(item);
        var size = 3;
        for (var i = 0; i < size; ++i) {
            items.add(itemService.create(owner.getId(), itemCreateDto));
        }
        var start = getCurrentTime().plusSeconds(1);
        var end = start.plusSeconds(1);

        var bookingLast = bookingService.create(user.getId(), BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build());
        bookingService.updateStatus(bookingLast.getId(), owner.getId(), true);
        var sleepTime = ChronoUnit.MILLIS.between(LocalDateTime.now(), end) + 1;
        Thread.sleep(sleepTime);
        itemService.createComment(user.getId(), item.getId(), CommentCreateDto.builder()
                .text("First comment")
                .build());
        itemService.createComment(user.getId(), item.getId(), CommentCreateDto.builder()
                .text("Second comment")
                .build());

        var booking = bookingService.create(user.getId(), BookingCreateDto.builder()
                .itemId(item.getId())
                .start(getCurrentTime().plusDays(3))
                .end(getCurrentTime().plusDays(4))
                .build());
        var bookingNext = bookingService.create(user.getId(), BookingCreateDto.builder()
                .itemId(item.getId())
                .start(getCurrentTime().plusDays(1))
                .end(getCurrentTime().plusDays(2))
                .build());
        bookingService.updateStatus(booking.getId(), owner.getId(), true);
        bookingService.updateStatus(bookingNext.getId(), owner.getId(), true);

        result = itemService.getAll(user.getId(), 0, null);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), equalTo(usersItem.getName()));
        assertThat(result.get(0).getDescription(), equalTo(usersItem.getDescription()));
        assertThat(result.get(0).getAvailable(), equalTo(usersItem.getAvailable()));
        assertThat(result.get(0).getComments(), hasSize(0));
        assertThat(result.get(0).getLastBooking(), nullValue());
        assertThat(result.get(0).getNextBooking(), nullValue());

        result = itemService.getAll(owner.getId(), 0, size);
        assertThat(result, hasSize(size));
        assertThat(result.get(0).getName(), equalTo(item.getName()));
        assertThat(result.get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(result.get(0).getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.get(0).getComments(), hasSize(2));
        assertThat(result.get(0).getLastBooking().getId(), equalTo(bookingLast.getId()));
        assertThat(result.get(0).getLastBooking().getStart(), equalTo(bookingLast.getStart()));
        assertThat(result.get(0).getLastBooking().getEnd(), equalTo(bookingLast.getEnd()));
        assertThat(result.get(0).getNextBooking().getId(), equalTo(bookingNext.getId()));
        assertThat(result.get(0).getNextBooking().getStart(), equalTo(bookingNext.getStart()));
        assertThat(result.get(0).getNextBooking().getEnd(), equalTo(bookingNext.getEnd()));
        for (var i = 1; i < size; ++i) {
            assertThat(result.get(i).getName(), equalTo(items.get(i).getName()));
            assertThat(result.get(i).getDescription(), equalTo(items.get(i).getDescription()));
            assertThat(result.get(i).getAvailable(), equalTo(items.get(i).getAvailable()));
            assertThat(result.get(i).getComments(), hasSize(0));
            assertThat(result.get(i).getLastBooking(), nullValue());
            assertThat(result.get(i).getNextBooking(), nullValue());
        }
    }

    @Test
    public void searchOk() {
        var owner = userService.create(ownerDto);

        var text = "text";

        var result = itemService.search(text, 0, null);
        assertThat(result, hasSize(0));

        var itemWithTextInNameDto = ItemDto.builder()
                .name("it" + text + "em")
                .description("item_description")
                .available(true)
                .build();

        var itemWithTextInDescriptionDto = ItemDto.builder()
                .name("item")
                .description("item_description" + text)
                .available(true)
                .build();

        var itemWithTextInDescriptionAndNameDto = ItemDto.builder()
                .name(text + "item")
                .description("item_desc" + text + "ription" + text)
                .available(true)
                .build();

        itemService.create(owner.getId(), itemCreateDto);
        var itemWithTextInName = itemService.create(owner.getId(), itemWithTextInNameDto);
        var itemWithTextInDescription = itemService.create(owner.getId(), itemWithTextInDescriptionDto);
        var itemWithTextInDescriptionAndName = itemService.create(owner.getId(), itemWithTextInDescriptionAndNameDto);

        result = itemService.search("", 0, null);
        assertThat(result, hasSize(0));

        var from = 0;
        var size = 2;
        result = itemService.search(text, from, size);
        assertThat(result, hasSize(size));
        assertThat(result.get(0).getId(), equalTo(itemWithTextInName.getId()));
        assertThat(result.get(0).getName(), equalTo(itemWithTextInNameDto.getName()));
        assertThat(result.get(0).getDescription(), equalTo(itemWithTextInNameDto.getDescription()));
        assertThat(result.get(0).getAvailable(), equalTo(itemWithTextInNameDto.getAvailable()));

        assertThat(result.get(1).getId(), equalTo(itemWithTextInDescription.getId()));
        assertThat(result.get(1).getName(), equalTo(itemWithTextInDescriptionDto.getName()));
        assertThat(result.get(1).getDescription(), equalTo(itemWithTextInDescriptionDto.getDescription()));
        assertThat(result.get(1).getAvailable(), equalTo(itemWithTextInDescriptionDto.getAvailable()));

        result = itemService.search(text, 0, null);
        assertThat(result, hasSize(3));
        assertThat(result.get(0).getId(), equalTo(itemWithTextInName.getId()));
        assertThat(result.get(0).getName(), equalTo(itemWithTextInNameDto.getName()));
        assertThat(result.get(0).getDescription(), equalTo(itemWithTextInNameDto.getDescription()));
        assertThat(result.get(0).getAvailable(), equalTo(itemWithTextInNameDto.getAvailable()));

        assertThat(result.get(1).getId(), equalTo(itemWithTextInDescription.getId()));
        assertThat(result.get(1).getName(), equalTo(itemWithTextInDescriptionDto.getName()));
        assertThat(result.get(1).getDescription(), equalTo(itemWithTextInDescriptionDto.getDescription()));
        assertThat(result.get(1).getAvailable(), equalTo(itemWithTextInDescriptionDto.getAvailable()));

        assertThat(result.get(2).getId(), equalTo(itemWithTextInDescriptionAndName.getId()));
        assertThat(result.get(2).getName(), equalTo(itemWithTextInDescriptionAndNameDto.getName()));
        assertThat(result.get(2).getDescription(), equalTo(itemWithTextInDescriptionAndNameDto.getDescription()));
        assertThat(result.get(2).getAvailable(), equalTo(itemWithTextInDescriptionAndNameDto.getAvailable()));
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

}