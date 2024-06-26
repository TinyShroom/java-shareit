package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.RequestCreateDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestClientTest {

    @MockBean
    private final RestTemplate restTemplate;

    private static RequestClient requestClient;

    @BeforeEach
    public void setUp() {
        requestClient = new RequestClient(restTemplate);
    }

    @Test
    public void createOk() {
        var userId = 1L;
        var requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
        Mockito.when(restTemplate.exchange("", HttpMethod.POST,
                        new HttpEntity<>(requestCreateDto, defaultHeaders(userId)), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var result = requestClient.create(userId, requestCreateDto);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getByIdOk() {
        var userId = 1L;
        var requestId = 1L;
        Mockito.when(restTemplate.exchange("/" + requestId, HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders(userId)), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var result = requestClient.getById(userId, requestId);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getByUserIdOk() {
        var userId = 1L;
        Mockito.when(restTemplate.exchange("", HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders(userId)), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var result = requestClient.getByUserId(userId);
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
        Mockito.when(restTemplate.exchange("/all?from={from}&size={size}", HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders(userId)), Object.class, parameters))
                .thenReturn(ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList()));
        var result = requestClient.getAll(userId, parameters);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
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