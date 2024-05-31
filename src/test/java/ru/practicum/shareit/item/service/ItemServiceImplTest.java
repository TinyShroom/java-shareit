package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingShort;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.TestBookingMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.util.PageRequestWithOffset;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final EntityManager em;
    private final CommentMapper commentMapper;
    private final TestBookingMapper testBookingMapper;

    private ItemDto itemCreateDto;

    private final long ownerId = 1;
    private final long bookerId = 2;
    private final long userId = 3;
    private final long unknownUserId = 99999;

    private final long requestWithoutItemsId = 1;
    private final long unknownRequestId = 99999;

    private final long itemIdSecond = 2;
    private final long itemWithoutBooking = 4;
    private final long unknownItemId = 99999;


    @BeforeEach
    public void setUp() {
        itemCreateDto = ItemDto.builder()
                .name("item")
                .description("item_description")
                .available(true)
                .build();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createOk() {
        var itemDto = itemService.create(ownerId, itemCreateDto);

        assertThat(itemDto.getName(), equalTo(itemCreateDto.getName()));
        assertThat(itemDto.getDescription(), equalTo(itemCreateDto.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(itemCreateDto.getAvailable()));
        assertThat(itemDto.getRequestId(), equalTo(itemCreateDto.getRequestId()));

        var itemQuery = em.createQuery("select i from Item i where i.id = :id", Item.class);
        var itemResult = itemQuery.setParameter("id", itemDto.getId())
                .getSingleResult();

        assertThat(itemResult.getId(), equalTo(itemDto.getId()));
        assertThat(itemResult.getOwner().getId(), equalTo(ownerId));
        assertThat(itemResult.getRequest(), nullValue());
        assertThat(itemResult.getName(), equalTo(itemDto.getName()));
        assertThat(itemResult.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemResult.getAvailable(), equalTo(itemDto.getAvailable()));

        var requestQuery = em.createQuery("select r from Request r where r.id = :id", Request.class);
        var requestResult = requestQuery.setParameter("id", requestWithoutItemsId)
                .getSingleResult();

        var itemCreateDtoWithRequest = ItemDto.builder()
                .name("item_rq")
                .description("description_rq")
                .available(true)
                .requestId(requestResult.getId())
                .build();
        var itemDtoRq = itemService.create(ownerId, itemCreateDtoWithRequest);

        assertThat(itemDtoRq.getName(), equalTo(itemCreateDtoWithRequest.getName()));
        assertThat(itemDtoRq.getDescription(), equalTo(itemCreateDtoWithRequest.getDescription()));
        assertThat(itemDtoRq.getAvailable(), equalTo(itemCreateDtoWithRequest.getAvailable()));
        assertThat(itemDtoRq.getRequestId(), equalTo(requestWithoutItemsId));

        itemResult = itemQuery.setParameter("id", itemDtoRq.getId())
                .getSingleResult();

        assertThat(itemResult.getId(), equalTo(itemDtoRq.getId()));
        assertThat(itemResult.getOwner().getId(), equalTo(ownerId));
        assertThat(itemResult.getRequest().getId(), equalTo(itemDtoRq.getRequestId()));
        assertThat(itemResult.getName(), equalTo(itemDtoRq.getName()));
        assertThat(itemResult.getDescription(), equalTo(itemDtoRq.getDescription()));
        assertThat(itemResult.getAvailable(), equalTo(itemDtoRq.getAvailable()));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createUnknownOwnerFail() {
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.create(unknownUserId, itemCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createUnknownRequestFail() {
        itemCreateDto.setRequestId(unknownRequestId);
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.create(ownerId, itemCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.REQUEST_NOT_FOUND.getFormatMessage(unknownRequestId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateOk() {

        var itemResult = em.createQuery("select i from Item i", Item.class)
                .getResultList().stream()
                .findAny()
                .get();

        var newDescription = "new " + itemResult.getDescription();
        var updateItemDto = ItemDto.builder()
                .id(itemResult.getId())
                .description(newDescription)
                .build();

        var itemDto = itemService.update(ownerId, updateItemDto);

        assertThat(itemDto.getName(), equalTo(itemResult.getName()));
        assertThat(itemDto.getDescription(), equalTo(updateItemDto.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(itemResult.getAvailable()));
        if (itemResult.getRequest() != null) {
            assertThat(itemDto.getRequestId(), equalTo(itemResult.getRequest().getId()));
        } else {
            assertThat(itemDto.getRequestId(), nullValue());
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateUnknownUserFail() {
        var itemResult = em.createQuery("select i from Item i", Item.class)
                .getResultList().stream()
                .findAny()
                .get();

        var updateItemDto = ItemDto.builder()
                .id(itemResult.getId())
                .description("new description")
                .build();

        var exception = assertThrows(NotFoundException.class,
                () -> itemService.update(unknownUserId, updateItemDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateUnknownItemFail() {
        var itemDto = ItemDto.builder()
                .id(unknownItemId)
                .build();
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.update(ownerId, itemDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(itemDto.getId())));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateNotOwnerFail() {
        var itemResult = em.createQuery("select i from Item i", Item.class)
                .getResultList().stream()
                .findAny()
                .get();
        var updateItemDto = ItemDto.builder()
                .id(itemResult.getId())
                .description("new description")
                .build();

        var exception = assertThrows(AccessDeniedException.class,
                () -> itemService.update(userId, updateItemDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.OWNER_UPDATE.getFormatMessage(userId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void deleteOk() {
        var itemResult = em.createQuery("select i from Item i", Item.class)
                .getResultList().stream()
                .findAny()
                .get();
        itemService.delete(ownerId, itemResult.getId());
        assertThrows(NoResultException.class,
                () -> em.createQuery("select i from Item i where i.id = :id", Item.class)
                        .setParameter("id", itemResult.getId())
                        .getSingleResult());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void deleteUnknownUserFail() {
        var itemResult = em.createQuery("select i from Item i", Item.class)
                .getResultList().stream()
                .findAny()
                .get();
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.delete(unknownUserId, itemResult.getId()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void deleteUnknownItemFail() {
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.delete(ownerId, unknownItemId));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(unknownItemId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void deleteUserNotOwnerFail() {
        var itemResult = em.createQuery("select i from Item i", Item.class)
                .getResultList().stream()
                .findAny()
                .get();
        var exception = assertThrows(AccessDeniedException.class,
                () -> itemService.delete(userId, itemResult.getId()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.OWNER_DELETE.getFormatMessage(userId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createCommentOk() {
        var booking = em.createQuery("select b from Booking b where b.status = 'APPROVED'", Booking.class)
                .getResultList().stream()
                .filter(b -> b.getEnd().isBefore(getCurrentTime()))
                .findAny()
                .get();
        var text = "Positive comment";
        var comment = itemService.createComment(booking.getBooker().getId(), booking.getItem().getId(),
                CommentCreateDto.builder()
                        .text(text)
                        .build());
        assertThat(comment.getText(), equalTo(text));
        assertThat(comment.getAuthorName(), equalTo(booking.getBooker().getName()));
        var query = em.createQuery("select c from Comment c where c.id = :id", Comment.class);
        var result = query.setParameter("id", comment.getId())
                .getSingleResult();

        assertThat(result.getId(), equalTo(comment.getId()));
        assertThat(result.getAuthor().getName(), equalTo(comment.getAuthorName()));
        assertThat(result.getText(), equalTo(comment.getText()));
        assertThat(result.getCreated(), equalTo(comment.getCreated()));
        assertThat(result.getItem().getId(), equalTo(booking.getItem().getId()));
        assertThat(result.getAuthor().getId(), equalTo(booking.getBooker().getId()));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createCommentWithoutBookingFail() {
        var bookingFuture = em.createQuery("select b from Booking b where b.status = 'APPROVED'", Booking.class)
                .getResultList().stream()
                .filter(b -> b.getStart().isAfter(getCurrentTime()))
                .findAny()
                .get();

        var bookingRejected = em.createQuery("select b from Booking b where b.status = 'REJECTED'", Booking.class)
                .getResultList().stream()
                .filter(b -> b.getEnd().isBefore(getCurrentTime()))
                .findAny()
                .get();

        var commentCreateDto = CommentCreateDto.builder()
                .text("First comment")
                .build();
        var exception = assertThrows(AccessDeniedException.class,
                () -> itemService.createComment(bookerId, bookingRejected.getItem().getId(), commentCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.REVIEW_WITHOUT_BOOKING.getMessage()));

        exception = assertThrows(AccessDeniedException.class,
                () -> itemService.createComment(bookerId, bookingFuture.getItem().getId(), commentCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.REVIEW_WITHOUT_BOOKING.getMessage()));

        exception = assertThrows(AccessDeniedException.class,
                () -> itemService.createComment(userId, itemWithoutBooking, commentCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.REVIEW_WITHOUT_BOOKING.getMessage()));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createCommentUnknownItemFail() {
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(bookerId, unknownItemId, CommentCreateDto.builder()
                        .text("First comment")
                        .build()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(unknownItemId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createCommentUnknownUserFail() {
        var booking = em.createQuery("select b from Booking b where b.status = 'APPROVED'", Booking.class)
                .getResultList().stream()
                .filter(b -> b.getEnd().isBefore(getCurrentTime()))
                .findAny()
                .get();
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(unknownUserId, booking.getItem().getId(), CommentCreateDto.builder()
                        .text("First comment")
                        .build()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    public void findByIdOk() {
        var item = em.createQuery("select i from Item i where i.id = :id", Item.class)
                .setParameter("id", itemIdSecond)
                .getSingleResult();

        var bookings = em.createQuery("select b from Booking b where b.status = 'APPROVED' and b.item.id = :id",
                        Booking.class)
                .setParameter("id", item.getId())
                .getResultList();
        var bookingLast = bookings.stream()
                .filter(b -> b.getStart().isBefore(getCurrentTime()))
                .max(Comparator.comparing(b -> b.getStart().isBefore(getCurrentTime())))
                .orElse(null);
        var bookingNext = bookings.stream()
                .filter(b -> b.getStart().isAfter(getCurrentTime()))
                .min(Comparator.comparing(b -> b.getStart().isBefore(getCurrentTime())))
                .orElse(null);
        var comments = em.createQuery("select c from Comment c where c.item.id = :id", Comment.class)
                .setParameter("id", item.getId())
                .getResultStream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
        var result = itemService.findById(ownerId, item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.getComments(), hasSize(comments.size()));
        if (bookingLast == null) {
            assertThat(result.getLastBooking(), nullValue());
        } else {
            assertThat(result.getLastBooking().getId(), equalTo(bookingLast.getId()));
            assertThat(result.getLastBooking().getStart(), equalTo(bookingLast.getStart()));
            assertThat(result.getLastBooking().getEnd(), equalTo(bookingLast.getEnd()));
        }
        if (bookingNext == null) {
            assertThat(result.getNextBooking(), nullValue());
        } else {
            assertThat(result.getNextBooking().getId(), equalTo(bookingNext.getId()));
            assertThat(result.getNextBooking().getStart(), equalTo(bookingNext.getStart()));
            assertThat(result.getNextBooking().getEnd(), equalTo(bookingNext.getEnd()));
        }
        org.assertj.core.api.Assertions.assertThat(result.getComments())
                .usingRecursiveComparison()
                .isEqualTo(comments);
    }

    @Test
    public void findByIdFail() {
        var exception = assertThrows(NotFoundException.class,
                () -> itemService.findById(unknownUserId, unknownItemId));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(unknownItemId)));
    }

    @Test
    public void findAllOk() {
        var from = 1;
        var size = 2;
        var items = em.createQuery("select i from Item i", Item.class)
                .getResultStream()
                .sorted(Comparator.comparingLong(Item::getId))
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());

        var itemsId = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        var bookings = em.createQuery("select b from Booking b where b.status = 'APPROVED' and b.item.id in :id",
                        Booking.class)
                .setParameter("id", itemsId)
                .getResultStream()
                .map(testBookingMapper::toModel)
                .collect(Collectors.toList());
        Map<Long, BookingShort> lastBookings = new HashMap<>();
        Map<Long, BookingShort> nextBookings = new HashMap<>();
        var dateTime = getCurrentTime();
        for (var booking: bookings) {
            if (dateTime.isAfter(booking.getStart())) {
                lastBookings.putIfAbsent(booking.getItemId(), booking);
            } else {
                nextBookings.putIfAbsent(booking.getItemId(), booking);
            }
        }
        var comments = em.createQuery("select c from Comment c where c.item.id in :id", Comment.class)
                .setParameter("id", itemsId)
                .getResultStream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(),
                        Collectors.mapping(commentMapper::toDto, Collectors.toList())));
        var comparedItems = items.stream()
                .map(item -> itemMapper.toItemWithBookingsDto(item, lastBookings.get(item.getId()),
                        nextBookings.get(item.getId()), comments.getOrDefault(item.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("id"));
        var result = itemService.getAll(ownerId, pageable);
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(comparedItems);
    }

    @Test
    public void searchOk() {
        var text = "text";
        var from = 3;
        var size = 2;
        var items = em.createQuery("select i from Item i where" +
                        " lower(i.name) like lower(concat('%', :text, '%')) or" +
                        " lower(i.description) like lower(concat('%', :text, '%'))", Item.class)
                .setParameter("text", text)
                .getResultStream()
                .filter(Item::getAvailable)
                .skip(from / size * size)
                .limit(size)
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        Pageable pageable = PageRequestWithOffset.of(from, size);
        var result = itemService.search(text, pageable);
        assertThat(result, hasSize(items.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(items);
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

}