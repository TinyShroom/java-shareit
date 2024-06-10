package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingClientTest {

    @MockBean
    private final RestTemplate restTemplate;

    private static BookingClient bookingClient;

    @BeforeEach
    public void setUp() {
        bookingClient = new BookingClient(restTemplate);
    }

    @Test
    public void createOk() {
        var start = LocalDateTime.now().plusMonths(1);
        var bookingCreateDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(start)
                .end(start.plusDays(1))
                .build();
        Mockito.when(restTemplate.exchange("", HttpMethod.POST,
                        new HttpEntity<>(bookingCreateDto, defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(new Object()));
        var result = bookingClient.create(1L, bookingCreateDto);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.CREATED));
    }

    @Test
    public void updateStatusOk() {
        var start = LocalDateTime.now().plusMonths(1);
        Mockito.when(restTemplate.exchange("/" + 1 + "?approved={approved}", HttpMethod.PATCH,
                        new HttpEntity<>(null, defaultHeaders(1L)), Object.class, Map.of("approved", true)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(new Object()));
        var result = bookingClient.updateStatus(1L, 1L,true);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void getByIdOk() {
        Mockito.when(restTemplate.exchange("/" + 1, HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(new Object()));
        var result = bookingClient.getById(1L, 1L);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void getAllForUserOk() {
        Map<String, Object> parameters = Map.of(
                "state", BookingState.ALL,
                "from", 0,
                "size", 10
        );
        Mockito.when(restTemplate.exchange("?state={state}&from={from}&size={size}", HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders(1L)), Object.class, parameters))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of()));
        var result = bookingClient.getAllForUser(1L, parameters);
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void getAllForOwnerOk() {
        Map<String, Object> parameters = Map.of(
                "state", BookingState.ALL,
                "from", 0,
                "size", 10
        );
        Mockito.when(restTemplate.exchange("/owner?state={state}&from={from}&size={size}", HttpMethod.GET,
                        new HttpEntity<>(null, defaultHeaders(1L)), Object.class, parameters))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of()));
        var result = bookingClient.getAllForOwner(1L, parameters);
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