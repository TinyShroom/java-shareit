package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.util.PageRequestWithOffset;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    private static ObjectMapper mapper;
    private static final String CUSTOM_HEADER = "X-Sharer-User-Id";

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void postOk() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var end = start.plusDays(1);
        var bookerId = 1L;
        var request = BookingCreateDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();
        var response = BookingDto.builder()
                .id(1L)
                .booker(new BookerDto(bookerId))
                .item(new ItemShortDto(1L, "My drill"))
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .build();
        var mockRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, bookerId)
                .content(mapper.writeValueAsString(request));
        when(bookingService.create(bookerId, request))
                .thenReturn(response);
        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(response.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(response.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.start", is(response.getStart().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(response.getEnd().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(response.getStatus().name())));
    }

    @Test
    void postValidationFailBookerId() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var end = start.plusDays(1);
        var request = BookingCreateDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request));
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postUserNotFound() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var end = start.plusDays(1);
        var bookerId = 1L;
        var request = BookingCreateDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();
        var mockRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, bookerId)
                .content(mapper.writeValueAsString(request));
        when(bookingService.create(bookerId, request))
                .thenThrow(new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(bookerId)));
        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(ErrorMessages.USER_NOT_FOUND.getFormatMessage(bookerId))));
    }

    @Test
    void postItemNotFound() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var end = start.plusDays(1);
        var bookerId = 1L;
        var itemId = 1L;
        var request = BookingCreateDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();
        var mockRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, bookerId)
                .content(mapper.writeValueAsString(request));
        when(bookingService.create(bookerId, request))
                .thenThrow(new NotFoundException(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(itemId)));
        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(itemId))));
    }

    @Test
    void patchOk() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var ownerId = 1L;
        var response = BookingDto.builder()
                .id(1L)
                .booker(new BookerDto(2L))
                .item(new ItemShortDto(1L, "My drill"))
                .start(start)
                .end(start.plusDays(1))
                .status(BookingStatus.REJECTED)
                .build();
        var mockRequest = MockMvcRequestBuilders.patch("/bookings/1?approved=false")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, ownerId);
        when(bookingService.updateStatus(response.getId(), ownerId, false))
                .thenReturn(response);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(response.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(response.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.start", is(response.getStart().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(response.getEnd().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(response.getStatus().name())));
    }

    @Test
    void patchWithoutApprovedFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.patch("/bookings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, 1);
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void patchWithoutOwnerFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.patch("/bookings/1?approved=false")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getOk() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var userId = 1L;
        var response = BookingDto.builder()
                .id(1L)
                .booker(new BookerDto(2L))
                .item(new ItemShortDto(1L, "My drill"))
                .start(start)
                .end(start.plusDays(1))
                .status(BookingStatus.REJECTED)
                .build();
        var mockRequest = MockMvcRequestBuilders.get("/bookings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId);
        when(bookingService.findById(response.getId(), userId))
                .thenReturn(response);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(response.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(response.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.start", is(response.getStart().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(response.getEnd().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(response.getStatus().name())));
    }

    @Test
    void getWithoutUserFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.get("/bookings/1")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllForUserOk() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var bookerId = 2L;
        var response = List.of(
                BookingDto.builder()
                        .id(1L)
                        .booker(new BookerDto(bookerId))
                        .item(new ItemShortDto(1L, "My drill"))
                        .start(start)
                        .end(start.plusDays(1))
                        .status(BookingStatus.APPROVED)
                        .build(),
                BookingDto.builder()
                        .id(2L)
                        .booker(new BookerDto(bookerId))
                        .item(new ItemShortDto(2L, "Turbo drill"))
                        .start(start)
                        .end(start.plusDays(1))
                        .status(BookingStatus.APPROVED)
                        .build());
        var from = 0;
        var size = 10;
        var mockRequest = MockMvcRequestBuilders.get(String.format("/bookings?from=%d&size=%d", from, size))
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, bookerId)
                .param("state", BookingState.ALL.name())
                .param("from", String.valueOf(from))
                .param("size", String.valueOf(size));

        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        when(bookingService.findAllForUser(bookerId, BookingState.ALL, pageable))
                .thenReturn(response);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(response.size())));
        for (var state: BookingState.values()) {
            mockRequest = MockMvcRequestBuilders.get("/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(CUSTOM_HEADER, bookerId)
                    .param("state", BookingState.ALL.name())
                    .param("from", String.valueOf(from))
                    .param("size", String.valueOf(size));

            when(bookingService.findAllForUser(bookerId, state, pageable))
                    .thenReturn(response);
            mockMvc.perform(mockRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(response.size())));
        }
    }

    @Test
    void getAllForOwnerOk() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var ownerId = 1L;
        var response = List.of(
                BookingDto.builder()
                        .id(1L)
                        .booker(new BookerDto(2L))
                        .item(new ItemShortDto(1L, "My drill"))
                        .start(start)
                        .end(start.plusDays(1))
                        .status(BookingStatus.APPROVED)
                        .build(),
                BookingDto.builder()
                        .id(2L)
                        .booker(new BookerDto(3L))
                        .item(new ItemShortDto(2L, "Turbo drill"))
                        .start(start)
                        .end(start.plusDays(1))
                        .status(BookingStatus.APPROVED)
                        .build());
        var from = 0;
        var size = 10;
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("start").descending());
        var mockRequest = MockMvcRequestBuilders.get(String.format("/bookings/owner?from=%d&size=%d", from, size))
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, ownerId)
                .param("state", BookingState.ALL.name())
                .param("from", String.valueOf(from))
                .param("size", String.valueOf(size));

        when(bookingService.findAllForOwner(ownerId, BookingState.ALL, pageable))
                .thenReturn(response);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(response.size())));
        for (var state: BookingState.values()) {
            mockRequest = MockMvcRequestBuilders.get("/bookings/owner")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(CUSTOM_HEADER, ownerId)
                    .param("state", state.name())
                    .param("from", String.valueOf(from))
                    .param("size", String.valueOf(size));

            when(bookingService.findAllForOwner(ownerId, state, pageable))
                    .thenReturn(response);
            mockMvc.perform(mockRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(response.size())));
        }
    }
}