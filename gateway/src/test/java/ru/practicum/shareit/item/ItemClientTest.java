package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemClientTest {

    @MockBean
    private final RestTemplate restTemplate;

    private static ItemClient itemClient;

    @BeforeEach
    public void setUp() {
        itemClient = new ItemClient(restTemplate);
    }

    @Test
    public void createOk() {
        var userId = 1L;
        var itemDto = ItemDto.builder()
                .name("item")
                .description("item_description")
                .available(true)
                .build();
        Mockito.when(restTemplate.exchange("", HttpMethod.POST,
                        new HttpEntity<>(itemDto, defaultHeaders(userId)), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var result = itemClient.create(userId, itemDto);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getByIdOk() {
        var userId = 1L;
        var itemId = 1L;
        Mockito.when(restTemplate.exchange("/" + itemId, HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders(userId)), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(itemId)));
        var result = itemClient.getById(userId, itemId);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getAllOk() {
        var userId = 1L;
        var from = 0;
        var size = 10;
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );

        Mockito.when(restTemplate.exchange("?from={from}&size={size}", HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders(userId)), Object.class, parameters))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var result = itemClient.getAll(userId, parameters);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getUpdateOk() {
        var userId = 1L;
        var itemId = 1L;
        var itemDto = ItemDto.builder()
                .name("item")
                .description("item_description")
                .available(true)
                .build();
        Mockito.when(restTemplate.exchange("/" + itemId, HttpMethod.PATCH,
                        new HttpEntity<>(itemDto, defaultHeaders(userId)), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(itemId)));
        var result = itemClient.update(userId, itemId, itemDto);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void deleteOk() {
        var userId = 1L;
        var itemId = 1L;
        Mockito.when(restTemplate.exchange("/" + itemId, HttpMethod.DELETE,
                        new HttpEntity<>(null, defaultHeaders(userId)), Object.class))
                .thenReturn(ResponseEntity.noContent().build());
        var result = itemClient.delete(userId, itemId);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));
    }

    @Test
    public void searchOk() {
        var userId = 1L;
        var from = 0;
        var size = 10;
        String text = "text";
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );

        Mockito.when(restTemplate.exchange("/search?text={text}&from={from}&size={size}", HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders(null)), Object.class, parameters))
                .thenReturn(ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList()));
        var result = itemClient.search(parameters);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void createCommentOk() {
        var userId = 1L;
        var itemId = 1L;
        var comment = CommentCreateDto.builder()
                .text("comment")
                .build();
        Mockito.when(restTemplate.exchange("/" + itemId + "/comment", HttpMethod.POST,
                        new HttpEntity<>(comment, defaultHeaders(userId)), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(itemId)));
        var result = itemClient.createComment(userId, itemId, comment);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }
}