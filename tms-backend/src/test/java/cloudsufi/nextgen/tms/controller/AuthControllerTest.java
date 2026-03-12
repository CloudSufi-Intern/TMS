package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.LoginRequestDTO;
import cloudsufi.nextgen.tms.dto.LoginResponseDTO;
import cloudsufi.nextgen.tms.dto.SignUpRequestDTO;
import cloudsufi.nextgen.tms.dto.SignUpResponseDTO;
import cloudsufi.nextgen.tms.enums.Role;
import cloudsufi.nextgen.tms.exception.AuthenticationException;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import cloudsufi.nextgen.tms.service.AuthService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web Layer tests for the Auth Controller.
 * This test suite verifies the HTTP routing, request validation, and exception handling.
 *
 * @author Yashas Yadav
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Verifies that the Controller returns 201 Created on a valid sign-up request.
     */
    @Test
    @DisplayName("POST /api/auth/signup - Should return 201 Created for a valid sign-up request")
    void signUp_whenValidRequest_shouldReturn201() throws Exception {

        SignUpRequestDTO request = new SignUpRequestDTO();
        request.setUsername("yashascs");
        request.setEmail("yashas@cs.com");
        request.setPassword("password123");
        request.setPhoneNo("1234567890");
        request.setRole(Role.ENGINEERING);

        SignUpResponseDTO response = SignUpResponseDTO.builder()
                .id(1L)
                .username("yashascs")
                .email("yashas@cs.com")
                .role(Role.ENGINEERING)
                .createdAt(LocalDateTime.now())
                .build();

        when(authService.signUp(any(SignUpRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * Verifies that the Controller returns 400 Bad Request when email already exists.
     */
    @Test
    @DisplayName("POST /api/auth/signup - Should return 400 Bad Request when email already exists")
    void signUp_whenEmailAlreadyExists_shouldReturnBadRequest() throws Exception {

        SignUpRequestDTO request = new SignUpRequestDTO();
        request.setUsername("yashascs");
        request.setEmail("yashas@cs.com");
        request.setPassword("password123");
        request.setRole(Role.ENGINEERING);

        when(authService.signUp(any(SignUpRequestDTO.class)))
                .thenThrow(new BadRequestException("User with this email already exists."));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
                .andExpect(result -> assertEquals(
                        "User with this email already exists.",
                        result.getResolvedException().getMessage()));
    }

    /**
     * Verifies that the Controller returns 200 OK with a JWT token on valid login.
     */
    @Test
    @DisplayName("POST /api/auth/login - Should return 200 OK with JWT token on valid credentials")
    void login_whenValidCredentials_shouldReturn200WithToken() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("yashas@cs.com");
        request.setPassword("password123");

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token("mock.jwt.token")
                .tokenType("Bearer")
                .build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that the Controller returns 401 Unauthorized when credentials are invalid.
     */
    @Test
    @DisplayName("POST /api/auth/login - Should return 401 Unauthorized when credentials are invalid")
    void login_whenInvalidCredentials_shouldReturnUnauthorized() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("yashas@cs.com");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new AuthenticationException("Invalid email or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AuthenticationException))
                .andExpect(result -> assertEquals(
                        "Invalid email or password.",
                        result.getResolvedException().getMessage()));
    }
}
