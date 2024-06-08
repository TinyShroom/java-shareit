package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.PageRequestWithOffset;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    void getByIdOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var itemDto = getItemWithBookingsDto(itemId);
        var mockRequest = MockMvcRequestBuilders.get("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId);
        when(itemService.findById(userId, itemId))
                .thenReturn(itemDto);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.lastBooking.id", is(itemDto.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.id", is(itemDto.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.comments.size()", is(itemDto.getComments().size())));
    }

    @Test
    void getByIdWithoutUserIdFail() throws Exception {
        var itemId = 1L;
        var mockRequest = MockMvcRequestBuilders.get("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var items = List.of(getItemWithBookingsDto(itemId),
                ItemWithBookingsDto.builder()
                        .id(itemId + 1)
                        .name("item name")
                        .description("item description")
                        .available(true)
                        .build()
        );
        var from = 0;
        var size = 10;
        var mockRequest = MockMvcRequestBuilders.get("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .param("from", String.valueOf(from))
                .param("size", String.valueOf(size));
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("id"));
        when(itemService.getAll(userId, pageable))
                .thenReturn(items);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(items.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(items.get(1).getId()), Long.class));
    }

    @Test
    void getAllWithoutUserIdFail() throws Exception {
        var mockRequest = MockMvcRequestBuilders.get("/items")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var itemDto = getItemDto(itemId);
        var mockRequest = MockMvcRequestBuilders.patch("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .content(mapper.writeValueAsString(itemDto));
        when(itemService.update(userId, itemDto))
                .thenReturn(itemDto);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void updateWithoutUserIdFail() throws Exception {
        var itemId = 1L;
        var itemDto = getItemDto(itemId);
        var mockRequest = MockMvcRequestBuilders.patch("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(itemDto));
        mockMvc.perform(mockRequest)
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var mockRequest = MockMvcRequestBuilders.delete("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId);
        mockMvc.perform(mockRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    void searchOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var items = List.of(getItemDto(itemId),
                ItemDto.builder()
                        .id(itemId + 1)
                        .name("item name")
                        .description("item description")
                        .available(true)
                        .build()
        );
        var text = "item";
        var from = 0;
        var size = 10;
        var mockRequest = MockMvcRequestBuilders.get("/items/search")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .param("text", text)
                .param("from", String.valueOf(from))
                .param("size", String.valueOf(size));
        Pageable pageable = PageRequestWithOffset.of(from, size);
        when(itemService.search(text, pageable))
                .thenReturn(items);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(items.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(items.get(1).getId()), Long.class));
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
    void createCommentOk() throws Exception {
        var itemId = 1L;
        var userId = 1L;
        var text = "Good item";

        var commentCreateDto = CommentCreateDto.builder()
                .text(text)
                .build();
        var commentDto = CommentDto.builder()
                .id(1L)
                .authorName("name")
                .text(text)
                .created(getCurrentTime())
                .build();
        var mockRequest = MockMvcRequestBuilders.post("/items/" + itemId + "/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .header(CUSTOM_HEADER, userId)
                .content(mapper.writeValueAsString(commentCreateDto));
        when(itemService.createComment(userId, itemId, commentCreateDto))
                .thenReturn(commentDto);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class));
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    private ItemWithBookingsDto getItemWithBookingsDto(long itemId) {
        var current = getCurrentTime();
        return ItemWithBookingsDto.builder()
                .id(itemId)
                .name("item name")
                .description("item description")
                .available(true)
                .lastBooking(BookingShortDto.builder()
                        .id(1L)
                        .bookerId(2L)
                        .start(current.minusDays(1).minusMinutes(30))
                        .end(current.minusDays(1).plusMinutes(30))
                        .build())
                .nextBooking(BookingShortDto.builder()
                        .id(2L)
                        .bookerId(2L)
                        .start(current.plusDays(1).minusMinutes(30))
                        .end(current.plusDays(1).plusMinutes(30))
                        .build())
                .comments(List.of(
                        CommentDto.builder()
                                .id(1L)
                                .text("Positive comment")
                                .authorName("Booker name")
                                .created(current.minusHours(10))
                                .build(),
                        CommentDto.builder()
                                .id(2L)
                                .text("Another positive comment")
                                .authorName("Booker name")
                                .created(current.minusHours(9))
                                .build()
                ))
                .build();
    }

    private ItemDto getItemDto(long itemId) {
        return ItemDto.builder()
                .id(itemId)
                .name("item name")
                .description("item description")
                .available(true)
                .build();
    }
}