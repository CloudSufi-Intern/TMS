package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.GetUserResponse;
import cloudsufi.nextgen.tms.dto.UserRequestDTO;
import cloudsufi.nextgen.tms.dto.UserSuggestionDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.Role;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for the User Service Module.
 *
 * This test suite loads the full Spring Boot application context
 * and tests the Business Logic Layer (Service) directly.
 * The Persistence Layer (Repository) is mocked to isolate logic
 * from the actual database.
 *
 * @author Ansh Parnami
 */
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    private UserEntity mockUser;

    /**
     * Sets up a mock user entity before each test execution.
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
     * Verifies that the Service correctly fetches a user by ID.
     */
    @Test
    @DisplayName("Service - Should successfully fetch user by ID and skip username/email fallback queries")
    void getUser_whenValidIdProvided_shouldReturnUserResponse() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        GetUserResponse response = userService.getUser(1L, null, null);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("anshcs", response.getUsername());

        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
    }

    /**
     * Verifies fallback search by username.
     */
    @Test
    @DisplayName("Service - Should fallback to fetching user by Username when ID is null")
    void getUser_whenValidUsernameAndNullId_shouldReturnUserResponse() {

        when(userRepository.findByUsername("anshcs")).thenReturn(Optional.of(mockUser));

        GetUserResponse response = userService.getUser(null, "anshcs", null);

        assertNotNull(response);
        assertEquals("ansh@cs.com", response.getEmail());
    }

    /**
     * Verifies exception when user cannot be found.
     */
    @Test
    @DisplayName("Service - Should throw RuntimeException when user cannot be found")
    void getUser_whenUserDoesNotExist_shouldThrowException() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("no@email.com")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.getUser(99L, "unknown", "no@email.com")
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Verifies that a user is successfully created.
     */
    @Test
    @DisplayName("Service - Should successfully create a new user")
    void addUser_whenValidRequest_shouldSaveUser() {

        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("anshcs");
        request.setEmail("ansh@cs.com");
        request.setPhoneNo("1234567890");
        request.setPassword("password123");
        request.setRole(Role.DEVOPS);

        when(userRepository.existsByEmail("ansh@cs.com")).thenReturn(false);
        when(userRepository.existsByUsername("anshcs")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(mockUser);

        UserEntity savedUser = userService.addUser(request);

        assertNotNull(savedUser);
        assertEquals("anshcs", savedUser.getUsername());
        assertEquals("ansh@cs.com", savedUser.getEmail());

        verify(userRepository).save(any(UserEntity.class));
    }

    /**
     * Verifies exception when email already exists.
     */
    @Test
    @DisplayName("Service - Should throw exception when email already exists")
    void addUser_whenEmailAlreadyExists_shouldThrowException() {

        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("anshcs");
        request.setEmail("ansh@cs.com");

        when(userRepository.existsByEmail("ansh@cs.com")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                userService.addUser(request)
        );

        assertTrue(exception.getMessage().contains("email already exists"));

        verify(userRepository, never()).save(any());
    }

    /**
     * Verifies exception when username already exists.
     */
    @Test
    @DisplayName("Service - Should throw exception when username already exists")
    void addUser_whenUsernameAlreadyExists_shouldThrowException() {

        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("anshcs");
        request.setEmail("ansh@cs.com");

        when(userRepository.existsByEmail("ansh@cs.com")).thenReturn(false);
        when(userRepository.existsByUsername("anshcs")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                userService.addUser(request)
        );

        assertTrue(exception.getMessage().contains("username already exists"));

        verify(userRepository, never()).save(any());
    }

    /**
     * Verifies that users are successfully searched by username prefix.
     * @author vishwasvaidya
     */
    @Test
    @DisplayName("Service - Should return paginated user suggestions when valid username is provided")
    void searchUsers_whenValidUsername_shouldReturnPagedResults() {

        String username = "vi";

        UserSuggestionDTO suggestion =
                new UserSuggestionDTO(1L, "vishwas");

        Page<UserSuggestionDTO> mockPage =
                new PageImpl<>(List.of(suggestion));

        when(userRepository.searchUsers(eq(username), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<UserSuggestionDTO> result =
                userService.searchUsers(username, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("vishwas", result.getContent().get(0).getUsername());
        assertEquals(1L, result.getContent().get(0).getId());

        verify(userRepository).searchUsers(eq(username), any(Pageable.class));
    }

    /**
     * Verifies that the service throws an IllegalArgumentException
     * when the provided username is null.
     *
     * This ensures input validation is enforced before querying
     * the repository layer.
     * @author vishwasvaidya
     */
    @Test
    @DisplayName("Service - Should throw exception when username is null")
    void searchUsers_whenUsernameIsNull_shouldThrowException() {

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () ->
                        userService.searchUsers(null, 0, 10)
                );

        assertTrue(exception.getMessage()
                .contains("Username must contain at least 2 characters"));

        verify(userRepository, never()).searchUsers(anyString(), any());
    }

    /**
     * Verifies that the service throws an IllegalArgumentException
     * when the provided username contains fewer than two characters.
     *
     * This prevents inefficient or unnecessary database queries
     * caused by overly short search keywords.
     * @author vishwasvaidya
     */
    @Test
    @DisplayName("Service - Should throw exception when username length is less than 2")
    void searchUsers_whenUsernameTooShort_shouldThrowException() {

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () ->
                        userService.searchUsers("v", 0, 10)
                );

        assertTrue(exception.getMessage()
                .contains("Username must contain at least 2 characters"));

        verify(userRepository, never()).searchUsers(anyString(), any());
    }
}
