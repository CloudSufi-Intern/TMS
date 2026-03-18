package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.LoginRequestDTO;
import cloudsufi.nextgen.tms.dto.LoginResponseDTO;
import cloudsufi.nextgen.tms.dto.SignUpRequestDTO;
import cloudsufi.nextgen.tms.dto.SignUpResponseDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.Role;
import cloudsufi.nextgen.tms.exception.AuthenticationException;
import cloudsufi.nextgen.tms.exception.DuplicateResourceException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import cloudsufi.nextgen.tms.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for the Auth Service Module.
 *
 * This test suite loads the full Spring Boot application context
 * and tests the Business Logic Layer (Service) directly.
 * The Persistence Layer (Repository) is mocked to isolate logic
 * from the actual database.
 *
 * @author Yashas Yadav
 */
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtUtil jwtUtil;

    private UserEntity mockUser;

    /**
     * Sets up a mock user entity before each test execution.
     */
    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setId(1L);
        mockUser.setUsername("yashascs");
        mockUser.setEmail("yashas@cs.com");
        mockUser.setPhoneNo("1234567890");
        mockUser.setPassword("$2a$10$hashedpassword");
        mockUser.setRole(Role.ENGINEERING);
        mockUser.setCreatedAt(LocalDateTime.now());
    }

    /**
     * Verifies that sign-up encodes the password with BCrypt before saving.
     */
    @Test
    @DisplayName("Service - Should successfully register user with BCrypt-encoded password")
    void signUp_whenValidRequest_shouldEncodePasswordAndSaveUser() {

        when(userRepository.findByEmail("yashas@cs.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("yashascs")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(mockUser);

        SignUpRequestDTO request = new SignUpRequestDTO();
        request.setUsername("yashascs");
        request.setEmail("yashas@cs.com");
        request.setPassword("password123");
        request.setPhoneNo("1234567890");
        request.setRole(Role.ENGINEERING);

        SignUpResponseDTO response = authService.signUp(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("yashascs", response.getUsername());
        assertEquals("yashas@cs.com", response.getEmail());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    /**
     * Verifies exception when email already exists during sign-up.
     */
    @Test
    @DisplayName("Service - Should throw exception when email already exists on sign-up")
    void signUp_whenEmailAlreadyExists_shouldThrowException() {

        SignUpRequestDTO request = new SignUpRequestDTO();
        request.setUsername("yashascs");
        request.setEmail("yashas@cs.com");
        request.setPassword("password123");
        request.setPhoneNo("1234567890");

        when(userRepository.findByEmail("yashas@cs.com")).thenReturn(Optional.of(mockUser));

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                authService.signUp(request)
        );

        assertTrue(exception.getMessage().contains("email already exists"));
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    /**
     * Verifies exception when username already exists during sign-up.
     */
    @Test
    @DisplayName("Service - Should throw exception when username already exists on sign-up")
    void signUp_whenUsernameAlreadyExists_shouldThrowException() {

        SignUpRequestDTO request = new SignUpRequestDTO();
        request.setUsername("yashascs");
        request.setEmail("yashas@cs.com");
        request.setPassword("password123");
        request.setPhoneNo("1234567890");

        when(userRepository.findByEmail("yashas@cs.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("yashascs")).thenReturn(Optional.of(mockUser));

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                authService.signUp(request)
        );

        assertTrue(exception.getMessage().contains("username already exists"));
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    /**
     * Verifies that the Service returns a JWT token on successful login.
     */
    @Test
    @DisplayName("Service - Should successfully return JWT token on valid login")
    void login_whenValidCredentials_shouldReturnJwtToken() {

        when(userRepository.findByEmail("yashas@cs.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtUtil.generateToken("yashas@cs.com")).thenReturn("mock.jwt.token");

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("yashas@cs.com");
        request.setPassword("password123");

        LoginResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());

        verify(jwtUtil).generateToken("yashas@cs.com");
    }

    /**
     * Verifies exception when email is not found during login.
     */
    @Test
    @DisplayName("Service - Should throw AuthenticationException when email not found on login")
    void login_whenEmailNotFound_shouldThrowException() {

        when(userRepository.findByEmail("ghost@cs.com")).thenReturn(Optional.empty());

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("ghost@cs.com");
        request.setPassword("password123");

        AuthenticationException exception = assertThrows(AuthenticationException.class, () ->
                authService.login(request)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    /**
     * Verifies exception when password does not match during login.
     */
    @Test
    @DisplayName("Service - Should throw AuthenticationException when password does not match on login")
    void login_whenPasswordDoesNotMatch_shouldThrowException() {

        when(userRepository.findByEmail("yashas@cs.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword")).thenReturn(false);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("yashas@cs.com");
        request.setPassword("wrongpassword");

        AuthenticationException exception = assertThrows(AuthenticationException.class, () ->
                authService.login(request)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }
}
