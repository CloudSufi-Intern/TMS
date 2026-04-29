package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.GetUserResponse;
import cloudsufi.nextgen.tms.dto.UpdateUserRequestDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

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
                .password(passwordEncoder.encode(request.getPassword()))
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
            throw new BadRequestException("Username must contain at least 2 characters");
        }
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.searchUsers(username, pageable);
    }

    /**
     * Updates the username and/or phone number of the currently authenticated user.
     *
     * This method supports partial updates:
     * - If only username is provided → updates username
     * - If only phone number is provided → updates phone number
     * - If both are provided → updates both
     *
     * Workflow:
     * 1. Extract username from JWT token
     * 2. Fetch user from database using username
     * 3. If username is provided:
     *      - Check uniqueness (only if changed)
     *      - Update username
     * 4. If phone number is provided:
     *      - Update phone number
     * 5. Save updated user
     * 6. Return mapped response DTO
     *
     * @param request DTO containing fields to update (partial allowed)
     * @return updated {@link GetUserResponse}
     *
     * @throws BadRequestException       if username already exists
     * @throws ResourceNotFoundException if user is not found
     *
     * @author Shubhanshu
     */
    public GetUserResponse updateUser(UpdateUserRequestDTO request) {

        // Step 1: Extract username from SecurityContext (JWT)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        log.info("Received request to update user with username: {}", currentUserEmail);

        // Step 2: Fetch existing user using username
        UserEntity existingUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> {
                    log.error("User update failed. No user found with username: {}", currentUserEmail);
                    return new ResourceNotFoundException("User not found.");
                });

        // Step 3: Update username if provided
        if (request.getUsername() != null && !request.getUsername().isBlank()) {

            String newUsername = request.getUsername();

            // Check uniqueness only if username is being changed
            if (!existingUser.getUsername().equals(newUsername)
                    && userRepository.existsByUsername(newUsername)) {

                log.warn("User update failed: Username already exists -> {}", newUsername);
                throw new BadRequestException("Username is already taken.");
            }

            existingUser.setUsername(newUsername);
        }

        // Step 4: Update phone number if provided
        if (request.getPhoneNo() != null && !request.getPhoneNo().isBlank()) {
            existingUser.setPhoneNo(request.getPhoneNo());
        }

        // Step 5: Save updated user
        UserEntity updatedUser = userRepository.save(existingUser);

        log.info("User updated successfully with username: {}", updatedUser.getUsername());

        // Step 6: Return response DTO
        return transformUserEntity(updatedUser);
    }


}
