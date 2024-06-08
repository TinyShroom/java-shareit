package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.request.dto.RequestCreateDto;

import static org.hamcrest.core.Is.is;
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