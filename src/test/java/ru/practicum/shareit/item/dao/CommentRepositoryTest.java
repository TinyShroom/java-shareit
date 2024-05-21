package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest(properties = {"db.url=jdbc:h2:mem:test;MODE=PostgreSQL", "db.driver=org.h2.Driver"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentRepositoryTest {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private User ownerCreate;
    private User userCreate;
    private List<Item> itemsCreate;
    private List<Booking> bookingsCreate;
    private List<Comment> commentsCreate;

    @BeforeEach
    public void setUp() {
        ownerCreate = new User();
        ownerCreate.setName("owner name");
        ownerCreate.setEmail("owner@mail.com");
        userCreate = new User();
        userCreate.setName("user name");
        userCreate.setEmail("user@mail.com");
        itemsCreate = new ArrayList<>();
        bookingsCreate = new ArrayList<>();
        commentsCreate = new ArrayList<>();
        var start = getCurrentTime().minusMinutes(10);
        for (var i = 0; i < 6; ++i) {
            var itemCreate = new Item();
            itemCreate.setName("item " + i);
            itemCreate.setDescription("item description " + i);
            itemCreate.setAvailable(true);
            itemsCreate.add(itemCreate);

            var bookingCreate = new Booking();
            bookingCreate.setStatus(BookingStatus.APPROVED);
            bookingCreate.setStart(start);
            bookingCreate.setEnd(start.plusMinutes(1));
            bookingsCreate.add(bookingCreate);

            var commentCreate = new Comment();
            commentCreate.setCreated(getCurrentTime());
            commentCreate.setText("Comment " + i + " to item " + i);
            commentsCreate.add(commentCreate);
            commentCreate = new Comment();
            commentCreate.setCreated(getCurrentTime());
            commentCreate.setText("Comment " + (i + 1) + " to item " + (i + 1));
            commentsCreate.add(commentCreate);
        }
    }

    @Test
    public void findAllByItemIdOk() {
        var owner = userRepository.save(ownerCreate);
        var user = userRepository.save(userCreate);
        Map<Long, List<Comment>> itemComments = new HashMap<>();
        for (var i = 0; i < itemsCreate.size(); ++i) {
            var itemCreate = itemsCreate.get(i);
            itemCreate.setOwner(owner);
            var item = itemRepository.save(itemCreate);
            var booking = bookingsCreate.get(i);
            booking.setBooker(user);
            booking.setItem(item);
            bookingRepository.save(booking);
            List<Comment> comments = new ArrayList<>();
            var comment = commentsCreate.get(i * 2);
            comment.setItem(item);
            comment.setAuthor(user);
            comments.add(commentRepository.save(comment));
            comment = commentsCreate.get(i * 2 + 1);
            comment.setItem(item);
            comment.setAuthor(user);
            comments.add(commentRepository.save(comment));
            itemComments.put(item.getId(), comments);
        }
        var itemId = itemComments.keySet().stream().findAny().get();
        var comments = itemComments.get(itemId);
        var result = commentRepository.findAllByItemId(itemId);
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(comments.get(0).getId()));
        assertThat(result.get(0).getText(), equalTo(comments.get(0).getText()));
        assertThat(result.get(0).getAuthorName(), equalTo(comments.get(0).getAuthor().getName()));
        assertThat(result.get(0).getItemId(), equalTo(comments.get(0).getItem().getId()));

        assertThat(result.get(1).getId(), equalTo(comments.get(1).getId()));
        assertThat(result.get(1).getText(), equalTo(comments.get(1).getText()));
        assertThat(result.get(1).getAuthorName(), equalTo(comments.get(1).getAuthor().getName()));
        assertThat(result.get(1).getItemId(), equalTo(comments.get(1).getItem().getId()));
    }

    @Test
    public void findAllByItemIdInOk() {
        var owner = userRepository.save(ownerCreate);
        var user = userRepository.save(userCreate);
        Map<Long, List<Comment>> itemComments = new HashMap<>();
        for (var i = 0; i < itemsCreate.size(); ++i) {
            var itemCreate = itemsCreate.get(i);
            itemCreate.setOwner(owner);
            var item = itemRepository.save(itemCreate);
            var booking = bookingsCreate.get(i);
            booking.setBooker(user);
            booking.setItem(item);
            bookingRepository.save(booking);
            List<Comment> comments = new ArrayList<>();
            var comment = commentsCreate.get(i * 2);
            comment.setItem(item);
            comment.setAuthor(user);
            comments.add(commentRepository.save(comment));
            comment = commentsCreate.get(i * 2 + 1);
            comment.setItem(item);
            comment.setAuthor(user);
            comments.add(commentRepository.save(comment));
            itemComments.put(item.getId(), comments);
        }
        var resultItemsSize = itemsCreate.size() / 2;
        var itemId = itemComments.keySet().stream()
                .limit(resultItemsSize)
                .collect(Collectors.toList());
        var result = commentRepository.findAllByItemIdIn(itemId);
        var resultCommentsSize = commentsCreate.size() / 2;
        assertThat(result, hasSize(resultCommentsSize));
        assertThat(result.get(0).getId(), equalTo(itemComments.get(itemId.get(0)).get(0).getId()));
        assertThat(result.get(0).getText(), equalTo(itemComments.get(itemId.get(0)).get(0).getText()));
        assertThat(result.get(0).getAuthorName(),
                equalTo(itemComments.get(itemId.get(0)).get(0).getAuthor().getName()));
        assertThat(result.get(0).getItemId(), equalTo(itemComments.get(itemId.get(0)).get(0).getItem().getId()));

        assertThat(result.get(resultCommentsSize - 1).getId(),
                equalTo(itemComments.get(itemId.get(resultItemsSize - 1)).get(1).getId()));
        assertThat(result.get(resultCommentsSize - 1).getText(),
                equalTo(itemComments.get(itemId.get(resultItemsSize - 1)).get(1).getText()));
        assertThat(result.get(resultCommentsSize - 1).getAuthorName(),
                equalTo(itemComments.get(itemId.get(resultItemsSize - 1)).get(1).getAuthor().getName()));
        assertThat(result.get(resultCommentsSize - 1).getItemId(),
                equalTo(itemComments.get(itemId.get(resultItemsSize - 1)).get(1).getItem().getId()));
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }
}