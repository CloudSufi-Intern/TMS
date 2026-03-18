package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.UserSuggestionDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.Role;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import cloudsufi.nextgen.tms.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web Layer tests for the User Controller.
 * This test suite verifies the HTTP routing, request validation, and exception
 * @author Ansh Parnami
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Verifies that the Controller returns a 400 Bad Request when no search parameters
     * (id, username, or email) are provided in the HTTP request.
     */
    @Test
    @DisplayName("GET /api/user - Should return 400 Bad Request when all search parameters are missing")
    void getUser_whenNoParametersProvided_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
                .andExpect(result -> assertEquals(
                        "At least one search parameter (id, username, or email) must be provided.",
                        result.getResolvedException().getMessage()));

        // Ensure the business layer is not invoked for bad requests
        verifyNoInteractions(userRepository);
    }

    /**
     * Verifies that the Controller successfully creates a new user.
     */
    @Test
    @DisplayName("POST /api/user - Should create a new user successfully")
    void addUser_shouldCreateUserSuccessfully() throws Exception {

        UserEntity user = UserEntity.builder()
                .id(1L)
                .username("smriti")
                .email("smriti@example.com")
                .role(Role.ENGINEERING)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/user/search - Should return user suggestions for matching username prefix")
    void searchUsers_whenValidUsernameProvided_shouldReturnSuggestions() throws Exception {
        String usernamePrefix = "jo";


        // Mocking UserSuggestionDTO interface using a simple anonymous class
        UserSuggestionDTO user1 = new UserSuggestionDTO() {
            @Override
            public Long getId() { return 1L; }
            @Override
            public String getUsername() { return "john"; }
        };
        UserSuggestionDTO user2 = new UserSuggestionDTO() {
            @Override
            public Long getId() { return 2L; }
            @Override
            public String getUsername() { return "josh"; }
        };


        List<UserSuggestionDTO> suggestions = List.of(user1, user2);
        Page<UserSuggestionDTO> page = new PageImpl<>(suggestions);


        // Mock the service
        when(userService.searchUsers(usernamePrefix, 0, 10)).thenReturn(page);


        // Perform the request and verify the response
        mockMvc.perform(get("/api/user/search")
                        .param("username", usernamePrefix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("john"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("josh"));


        verify(userService).searchUsers(usernamePrefix, 0, 10);
    }


}