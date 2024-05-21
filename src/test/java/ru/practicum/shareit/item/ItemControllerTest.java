package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;
    private static ObjectMapper mapper;
    private static final String CUSTOM_HEADER = "X-Sharer-User-Id";


    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void postOk() throws Exception {
        var content = "{\"name\": \"item name\",\"description\": \"item description\",\"available\":true}";
        var answer = "{\"id\":1,\"name\": \"item name\",\"description\": \"item description\",\"available\":true}";
        long userId = 1;
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .content(content);
        when(itemService.create(userId, mapper.readValue(content, ItemDto.class)))
                .thenReturn(mapper.readValue(answer, ItemDto.class));
        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("item name")))
                .andExpect(jsonPath("$.description", is("item description")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void postValidationFailName() throws Exception {
        var content = "{\"description\": \"item description\",\"available\":true}";
        long userId = 1;
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .content(content);
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void postValidationFailDescription() throws Exception {
        var content = "{\"name\": \"item name\",\"available\":true}";
        long userId = 1;
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .content(content);
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void postValidationFailAvailable() throws Exception {
        var content = "{\"name\": \"item name\",\"description\": \"item description\"}";
        long userId = 1;
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .content(content);
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }
}