package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.Role;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

}