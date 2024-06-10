package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserClientTest {

    @MockBean
    private final RestTemplate restTemplate;

    private static UserClient userClient;

    @BeforeEach
    public void setUp() {
        userClient = new UserClient(restTemplate);
    }

    @Test
    public void createOk() {
        var userDto = UserDto.builder()
                .name("user")
                .email("user@mailcom")
                .build();
        Mockito.when(restTemplate.exchange("", HttpMethod.POST,
                        new HttpEntity<>(userDto, defaultHeaders()), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(new Object()));
        var result = userClient.create(userDto);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.CREATED));
    }

    @Test
    public void getByIdOk() {
        var userId = 1L;
        Mockito.when(restTemplate.exchange("/" + userId, HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders()), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var result = userClient.getById(userId);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getAllOk() {
        Mockito.when(restTemplate.exchange("", HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders()), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList()));
        var result = userClient.getAll();
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void updateOk() {
        var userId = 1L;
        var userDto = UserDto.builder()
                .name("user")
                .email("user@mailcom")
                .build();
        Mockito.when(restTemplate.exchange("/" + userId, HttpMethod.PATCH,
                        new HttpEntity<>(userDto, defaultHeaders()), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var result = userClient.update(userId, userDto);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void deleteOk() {
        var userId = 1L;
        Mockito.when(restTemplate.exchange("/" + userId, HttpMethod.DELETE,
                        new HttpEntity<>(null, defaultHeaders()), Object.class))
                .thenReturn(ResponseEntity.noContent().build());
        var result = userClient.delete(userId);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}