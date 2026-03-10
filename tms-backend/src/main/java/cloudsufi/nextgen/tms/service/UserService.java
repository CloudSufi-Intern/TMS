package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.GetUserResponse;
import cloudsufi.nextgen.tms.dto.UserRequestDTO;
import cloudsufi.nextgen.tms.dto.UserSuggestionDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for managing user-related business logic.
 * Handles creation, retrieval and transformation of User entities to DTOs.
 *
 * @author Ansh Parnami
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Creates a new user in the system.
     *
     * @param request DTO containing user creation details
     * @return the saved {@link UserEntity}
     * @throws BadRequestException if username or email already exists
     */
    public UserEntity addUser(UserRequestDTO request) {

        log.info("Received request to create user with username: {} and email: {}",
                request.getUsername(), request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User creation failed: Email already exists -> {}", request.getEmail());
            throw new BadRequestException("User with this email already exists.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("User creation failed: Username already exists -> {}", request.getUsername());
            throw new BadRequestException("User with this username already exists.");
        }

        UserEntity entity = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNo(request.getPhoneNo())
                .password(request.getPassword())
                .role(request.getRole())
                .build();

        UserEntity savedUser = userRepository.save(entity);

        log.info("User created successfully with id: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Retrieves a user by searching across multiple identifiers in order of priority:
     * ID, then Username, then Email.
     *
     * @param id       the user's unique ID (nullable)
     * @param username the user's unique username (nullable)
     * @param email    the user's unique email (nullable)
     * @return a {@link GetUserResponse} containing the mapped user data
     * @throws ResourceNotFoundException if no user is found matching any criteria
     * @throws BadRequestException if no search parameters are provided
     */
    public GetUserResponse getUser(Long id, String username, String email) {

        boolean hasId = id != null;
        boolean hasUsername = username != null && !username.isBlank();
        boolean hasEmail = email != null && !email.isBlank();

        if (!hasId && !hasUsername && !hasEmail) {
            log.warn("Service validation failed: No search parameters provided");
            throw new BadRequestException(
                    "At least one search parameter (id, username, or email) must be provided.");
        }

        return Optional.ofNullable(id)
                .flatMap(userRepository::findById)
                .or(() -> Optional.ofNullable(username).flatMap(userRepository::findByUsername))
                .or(() -> Optional.ofNullable(email).flatMap(userRepository::findByEmail))
                .map(userEntity -> {
                    log.info("User found successfully for ID/Username/Email: {}/{}/{}", id, username, email);
                    return this.transformUserEntity(userEntity);
                })
                .orElseThrow(() -> {
                    log.error("User lookup failed. No user found for ID: {}, Username: {}, Email: {}", id, username, email);
                    return new ResourceNotFoundException("User not found with provided ID, Username, or Email.");
                });
    }

    /**
     * Maps a UserEntity to a GetUserResponse DTO.
     *
     * @param userEntity the persistence entity to transform
     * @return the populated DTO
     */
    private GetUserResponse transformUserEntity(UserEntity userEntity) {

        return GetUserResponse.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .phoneNo(userEntity.getPhoneNo())
                .role(userEntity.getRole())
                .build();
    }
    /**
     * Searches users whose usernames start with the given keyword.
     *
     * This method supports pagination for efficient database querying.
     *
     * @param username The username prefix used for searching users.
     * @param page     The page number to retrieve (0-based index).
     * @param size     The number of records to return per page.
     *
     * @return A paginated {@link Page} containing {@link UserSuggestionDTO}
     * objects representing matching users.
     * @author vishwasvaidya
     */

    public Page<UserSuggestionDTO> searchUsers(String username, int page, int size) {
        if (username == null || username.trim().length() < 2) {
            throw new IllegalArgumentException("Username must contain at least 2 characters");
        }
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.searchUsers(username, pageable);
    }
}
