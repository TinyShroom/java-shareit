package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private static ObjectMapper mapper;

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void postOk() throws Exception {
        var content = "{\"email\": \"user@mail.com\",\"name\": \"username\"}";
        var answer = "{\"email\": \"user@mail.com\",\"name\": \"username\",\"id\": 1}";

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        when(userService.create(mapper.readValue(content, UserDto.class)))
                .thenReturn(mapper.readValue(answer, UserDto.class));
        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("username")))
                .andExpect(jsonPath("$.email", is("user@mail.com")));

        content = "{\"email\": \"user@mail.com\"}";
        answer = "{\"email\": \"user@mail.com\",\"name\": null,\"id\": 2}";

        mockRequest = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        when(userService.create(mapper.readValue(content, UserDto.class)))
                .thenReturn(mapper.readValue(answer, UserDto.class));
        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.email", is("user@mail.com")));
    }

    @Test
    void postDoubleEmailFail() throws Exception {
        var content = "{\"email\": \"user@mail.com\",\"name\": \"username\"}";

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        when(userService.create(mapper.readValue(content, UserDto.class)))
                .thenThrow(new DataIntegrityViolationException(""));
        mockMvc.perform(mockRequest)
                .andExpect(status().isConflict());
    }

    @Test
    void patchOk() throws Exception {
        var content = "{\"email\": \"new@mail.com\"}";
        var answer = "{\"id\": 1,\"email\": \"new@mail.com\",\"name\": \"user name\"}";

        var mockRequest = MockMvcRequestBuilders.patch("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        when(userService.update(1, mapper.readValue(content, UserDto.class)))
                .thenReturn(mapper.readValue(answer, UserDto.class));
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("new@mail.com")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("user name")));

        content = "{\"name\": \"new name\"}";
        answer = "{\"id\": 1,\"email\": \"new@mail.com\",\"name\": \"new name\"}";

        mockRequest = MockMvcRequestBuilders.patch("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);

        when(userService.update(1, mapper.readValue(content, UserDto.class)))
                .thenReturn(mapper.readValue(answer, UserDto.class));
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("new@mail.com")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("new name")));
    }

    @Test
    void getByIdOk() throws Exception {
        var userId = 1L;
        var userDto = UserDto.builder()
                .id(userId)
                .name("username")
                .email("user@mail.com")
                .build();
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.get("/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON);
        when(userService.findById(userId))
                .thenReturn(userDto);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void getAllOk() throws Exception {
        var userId = 1L;
        var users = List.of(
                UserDto.builder()
                        .id(userId)
                        .name("user1name")
                        .email("user1@mail.com")
                        .build(),
                UserDto.builder()
                        .id(userId + 1)
                        .name("user2name")
                        .email("user2@mail.com")
                        .build()
        );
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.get("/users")
                .contentType(MediaType.APPLICATION_JSON);
        when(userService.getAll())
                .thenReturn(users);
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(users.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(users.get(1).getId()), Long.class));
    }

    @Test
    void deleteOk() throws Exception {
        var userId = 1L;
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.delete("/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(mockRequest)
                .andExpect(status().isNoContent());
    }
}