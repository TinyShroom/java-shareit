package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.request.dto.RequestCreateDto;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RequestClient requestClient;
    private static ObjectMapper mapper;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void postOk() throws Exception {
        var userId = 1L;
        var createDto = RequestCreateDto.builder()
                .description("request description")
                .build();
        var mockRequest = MockMvcRequestBuilders.post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId)
                .content(mapper.writeValueAsString(createDto));
        when(requestClient.create(userId, createDto))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIdOk() throws Exception {
        var userId = 1L;
        var requestId = 1L;
        var mockRequest = MockMvcRequestBuilders.get("/requests/" + requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId);
        when(requestClient.getById(userId, requestId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    void getForUserOk() throws Exception {
        var userId = 1L;
        var mockRequest = MockMvcRequestBuilders.get("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId);
        when(requestClient.getByUserId(userId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllOk() throws Exception {
        var userId = 1L;
        var from = 0;
        var size = 10;
        var mockRequest = MockMvcRequestBuilders.get(String.format("/requests/all?from=%d&size=%d", from, size))
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId);
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        when(requestClient.getAll(userId, parameters))
                .thenReturn(ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList()));
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk());
    }

    @Test
    void postBlankDescriptionFail() throws Exception {
        var userId = 1L;
        var createDto = RequestCreateDto.builder()
                .description("")
                .build();
        var mockRequest = MockMvcRequestBuilders.post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId)
                .content(mapper.writeValueAsString(createDto));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));

        createDto = RequestCreateDto.builder()
                .description(null)
                .build();
        mockRequest = MockMvcRequestBuilders.post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId)
                .content(mapper.writeValueAsString(createDto));
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }


}