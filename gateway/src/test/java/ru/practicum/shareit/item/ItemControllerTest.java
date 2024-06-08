package ru.practicum.shareit.item;

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
import ru.practicum.shareit.exception.ErrorMessages;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemClient itemClient;
    private static ObjectMapper mapper;
    private static final String CUSTOM_HEADER = "X-Sharer-User-Id";


    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
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

    @Test
    void getNegativeFromFail() throws Exception {
        var userId = 1L;
        var mockRequest = MockMvcRequestBuilders.get("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .param("from", "-1")
                .param("size", "1");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getZeroSizeFail() throws Exception {
        var userId = 1L;
        var mockRequest = MockMvcRequestBuilders.get("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .param("from", "1")
                .param("size", "0");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchWithoutTextFail() throws Exception {
        var userId = 1L;
        var mockRequest = MockMvcRequestBuilders.get("/items/search")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId);
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void searchNegativeFromFail() throws Exception {
        var userId = 1L;
        var text = "item";
        var mockRequest = MockMvcRequestBuilders.get("/items/search")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .param("text", text)
                .param("from", "-1")
                .param("size", "1");
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchZeroSizeFail() throws Exception {
        var userId = 1L;
        var text = "item";
        var mockRequest = MockMvcRequestBuilders.get("/items/search")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .param("text", text)
                .param("from", "0")
                .param("size", "0");
        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest());
    }

}