package ru.practicum.shareit.user;

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

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

    private static ObjectMapper mapper;

    @BeforeAll
    public static void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void patchValidationFailEmail() throws Exception {

        var mockRequest = MockMvcRequestBuilders.patch("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"\",\"name\": \"User name\"}");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));

        mockRequest = MockMvcRequestBuilders.patch("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"usermail.com\",\"name\": \"User name\"}");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));

        mockRequest = MockMvcRequestBuilders.patch("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"usermail.com@\",\"name\": \"User name\"}");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }

    @Test
    void postValidationFailEmail() throws Exception {

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": null,\"name\": \"User name\"}");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));

        mockRequest = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"\",\"name\": \"User name\"}");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));

        mockRequest = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"usermail.com\",\"name\": \"User name\"}");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));

        mockRequest = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"usermail.com@\",\"name\": \"User name\"}");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(ErrorMessages.VALIDATION_EXCEPTION.getMessage())));
    }
}