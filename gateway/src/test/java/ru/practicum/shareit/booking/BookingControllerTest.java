package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.exception.ErrorMessages;

import java.time.LocalDateTime;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingClient bookingClient;
    private static ObjectMapper mapper;
    private static final String CUSTOM_HEADER = "X-Sharer-User-Id";

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void postValidationFailDates() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var end = start.plusDays(1);
        var bookerId = 1L;
        var request = BookingCreateDto.builder()
                .itemId(1L)
                .start(end)
                .end(start)
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, bookerId)
                .content(mapper.writeValueAsString(request));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));

        request.setStart(LocalDateTime.now().minusHours(1));
        request.setEnd(end);
        mockRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, bookerId)
                .content(mapper.writeValueAsString(request));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void postValidationFailItemId() throws Exception {
        var start = LocalDateTime.now().plusHours(1);
        var end = start.plusDays(1);
        var bookerId = 1L;
        var request = BookingCreateDto.builder()
                .itemId(null)
                .start(start)
                .end(end)
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, bookerId)
                .content(mapper.writeValueAsString(request));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void getAllForUserUnknownStateFail() throws Exception {
        String errorState = "UNKNOWN";
        var mockRequest = MockMvcRequestBuilders.get("/bookings?state=" + errorState)
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, 2L);
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.UNKNOWN_STATE.getFormatMessage(errorState))));
    }

    @Test
    void getAllForUserFromFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.get("/bookings?from=-1&size=1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, 2L);
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllForUserSizeFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.get("/bookings?from=0&size=0")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, 2L);
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllForOwnerUnknownStateFail() throws Exception {
        String errorState = "UNKNOWN";
        var mockRequest = MockMvcRequestBuilders.get("/bookings/owner?state=" + errorState)
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, 2L);
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.UNKNOWN_STATE.getFormatMessage(errorState))));
    }

    @Test
    void getAllForOwnerFromFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.get("/bookings/owner?from=-1&size=1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, 1L);
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllForOwnerSizeFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.get("/bookings/owner?from=0&size=0")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, 1L);
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest());
    }

}