package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.GetUserResponse;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for managing user-related business logic.
 * Till Current Stage , Handles retrieval and transformation of User entities to DTOs.
 * @author Ansh Parnami
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;



    /**
     * Retrieves a user by searching across multiple identifiers in order of priority:
     * ID, then Username, then Email.
     * @param id       the user's unique ID (nullable)
     * @param username the user's unique username (nullable)
     * @param email    the user's unique email (nullable)
     * @return a {@link GetUserResponse} containing the mapped user data
     * @throws RuntimeException if no user is found matching any of the criteria
     * @throws IllegalArgumentException if no search parameters are provided
     */
    public GetUserResponse getUser(Long id, String username, String email) {
        boolean hasId = id != null;
        boolean hasUsername = username != null && !username.isBlank();
        boolean hasEmail = email != null && !email.isBlank();

        if (!hasId && !hasUsername && !hasEmail) {
            log.warn("Service validation failed: No search parameters provided");
            throw new BadRequestException("At least one search parameter (id, username, or email) must be provided.");

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
     * @param userEntity the persistence entity to transform
     * @return the populated DTO
     */
    private GetUserResponse transformUserEntity(UserEntity userEntity) {
        return GetUserResponse.builder().id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .phoneNo(userEntity.getPhoneNo())
                .role(userEntity.getRole()).build();
    }
}
