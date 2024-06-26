package ru.practicum.shareit.request;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.util.PageRequestWithOffset;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RequestService requestService;
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
        var createdDate = LocalDateTime.now();
        var answer = RequestDto.builder()
                .id(1L)
                .description(createDto.getDescription())
                .created(createdDate)
                .build();
        var mockRequest = MockMvcRequestBuilders.post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId)
                .content(mapper.writeValueAsString(createDto));
        when(requestService.create(userId, createDto))
                .thenReturn(answer);
        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(answer.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(answer.getDescription())))
                .andExpect(jsonPath("$.created", is(answer.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))));
    }

    @Test
    void postEmptyUserIdHeaderFail() throws Exception {
        var createDto = RequestCreateDto.builder()
                .description("description")
                .build();
        var mockRequest = MockMvcRequestBuilders.post("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createDto));
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getByIdOk() throws Exception {
        var userId = 1L;
        var requestId = 1L;
        var createdDate = LocalDateTime.now();
        var answer = RequestWithItemsDto.builder()
                .id(requestId)
                .description("request description")
                .created(createdDate)
                .items(Collections.emptyList())
                .build();
        var mockRequest = MockMvcRequestBuilders.get("/requests/" + requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId);
        when(requestService.findById(userId, requestId))
                .thenReturn(answer);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(answer.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(answer.getDescription())))
                .andExpect(jsonPath("$.created", is(answer.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.items", hasSize(answer.getItems().size())));
    }

    @Test
    void getEmptyUserIdHeaderFail() throws Exception {
        var requestId = 1L;
        var mockRequest = MockMvcRequestBuilders.get("/requests/" + requestId)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getForUserOk() throws Exception {
        var userId = 1L;
        var createdDate = LocalDateTime.now();
        var answer = List.of(
                RequestWithItemsDto.builder()
                        .id(1L)
                        .description("request description")
                        .created(createdDate)
                        .items(Collections.emptyList())
                        .build()
        );
        var mockRequest = MockMvcRequestBuilders.get("/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId);
        when(requestService.findByUserId(userId))
                .thenReturn(answer);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(answer.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(answer.get(0).getDescription())))
                .andExpect(jsonPath("$[0].created", is(answer.get(0).getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].items", hasSize(answer.get(0).getItems().size())));
    }

    @Test
    void getForUserEmptyUserIdHeaderFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.get("/requests")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllOk() throws Exception {
        var userId = 1L;
        var createdDate = LocalDateTime.now();
        var answer = List.of(
                RequestWithItemsDto.builder()
                        .id(1L)
                        .description("request description")
                        .created(createdDate)
                        .items(Collections.emptyList())
                        .build()
        );
        var from = 0;
        var size = 10;
        var mockRequest = MockMvcRequestBuilders.get(String.format("/requests/all?from=%d&size=%d", from, size))
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, userId);
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("created").descending());
        when(requestService.findAll(userId, pageable))
                .thenReturn(answer);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(answer.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(answer.get(0).getDescription())))
                .andExpect(jsonPath("$[0].created", is(answer.get(0).getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].items", hasSize(answer.get(0).getItems().size())));
    }

    @Test
    void getAllEmptyUserIdHeaderFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.get("/requests/all")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }
}