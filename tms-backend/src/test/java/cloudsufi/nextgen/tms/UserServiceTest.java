package cloudsufi.nextgen.tms;


import cloudsufi.nextgen.tms.dto.GetUserResponse;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.Role;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import cloudsufi.nextgen.tms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Tests for the User Service Module.
 *
 * This test suite loads the full Spring Boot application context.
 * using {@link MockMvc} and the Business Logic Layer (Service) directly. The Persistence Layer
 * (Repository) is mocked to isolate business and web logic from the actual database.
 *
 *  @author Ansh Parnami
 */
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    private UserEntity mockUser;

    /**
     * Sets up a mock user entity before each test execution to ensure
     * a clean state and avoid data leakage between tests.
     */
    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setId(1L);
        mockUser.setUsername("anshcs");
        mockUser.setEmail("ansh@cs.com");
        mockUser.setPhoneNo("1234567890");
        mockUser.setRole(Role.DEVOPS);
    }


    /**
     * Verifies that the Service correctly fetches a user by ID and maps it to a response DTO.
     * It also ensures that fallback database queries (username/email) are bypassed for performance.
     */
    @Test
    @DisplayName("Service - Should successfully fetch user by ID and skip username/email fallback queries")
    void getUser_whenValidIdProvided_shouldReturnUserResponse() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        GetUserResponse response = userService.getUser(1L, null, null);

        assertNotNull(response, "The returned response should not be null");
        assertEquals(1L, response.getId(), "The ID should match the requested ID");
        assertEquals("anshcs", response.getUsername(), "The username should match the mock data");

        // Verify short-circuit logic: If ID is found, do not query other fields
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
    }

    /**
     * Verifies that the Service correctly falls back to fetching a user by username
     * when the primary identifier (ID) is not provided.
     */
    @Test
    @DisplayName("Service - Should fallback to fetching user by Username when ID is null")
    void getUser_whenValidUsernameAndNullId_shouldReturnUserResponse() {

        when(userRepository.findByUsername("anshcs")).thenReturn(Optional.of(mockUser));

        GetUserResponse response = userService.getUser(null, "anshcs", null);

        assertNotNull(response, "The returned response should not be null");
        assertEquals("ansh@cs.com", response.getEmail(), "The email should match the mock data");
    }

    /**
     * Verifies that the Service throws a RuntimeException when a user cannot be found
     * using any of the provided fallback search parameters.
     */
    @Test
    @DisplayName("Service - Should throw RuntimeException when user cannot be found by ID, Username, or Email")
    void getUser_whenUserDoesNotExist_shouldThrowException() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("no@email.com")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.getUser(99L, "unknown", "no@email.com")
        );

        assertTrue(exception.getMessage().contains("User not found"),
                "Exception message should indicate the user was not found");
    }
}