package ru.practicum.shareit.item;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
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
    void postOk() throws Exception {
        var content = "{\"name\": \"item name\",\"description\": \"item description\",\"available\":true}";
        var answer = "{\"id\":1,\"name\": \"item name\",\"description\": \"item description\",\"available\":true}";
        long userId = 1;
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .content(content);
        when(itemClient.create(userId, mapper.readValue(content, ItemDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(mapper.readValue(answer, ItemDto.class)));
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

    @Test
    void getByIdOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var mockRequest = MockMvcRequestBuilders.get("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId);
        when(itemClient.getById(userId, itemId))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.ITEM_NOT_FOUND.getFormatMessage(itemId)));
        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound());
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
    void getAllOk() throws Exception {
        var userId = 1L;
        var from = 0;
        var size = 10;

        var mockRequest = MockMvcRequestBuilders.get("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .param("from", String.valueOf(from))
                .param("size", String.valueOf(size));
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        when(itemClient.getAll(userId, parameters))
                .thenReturn(ResponseEntity.status(HttpStatus.OK)
                        .body(Collections.emptyList()));
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk());
    }

    @Test
    void updateOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var itemDto = ItemDto.builder()
                .id(itemId)
                .name("item name")
                .description("item description")
                .available(true)
                .build();
        var mockRequest = MockMvcRequestBuilders.patch("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .content(mapper.writeValueAsString(itemDto));
        when(itemClient.update(userId, itemId, itemDto))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(itemDto));
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void deleteOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var mockRequest = MockMvcRequestBuilders.delete("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId);
        when(itemClient.delete(userId, itemId))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(mockRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    void searchOk() throws Exception {
        var userId = 1L;
        var text = "item";
        var from = 0;
        var size = 10;
        var mockRequest = MockMvcRequestBuilders.get("/items/search")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .param("text", text)
                .param("from", String.valueOf(from))
                .param("size", String.valueOf(size));
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );

        when(itemClient.search(parameters))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(Collections.emptyList()));
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk());
    }

    @Test
    void createCommentOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var text = "Good item";

        var commentCreateDto = CommentCreateDto.builder()
                .text(text)
                .build();
        var mockRequest = MockMvcRequestBuilders.post("/items/" + itemId + "/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .content(mapper.writeValueAsString(commentCreateDto));
        when(itemClient.createComment(userId, itemId, commentCreateDto))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound());
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