package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.GetUserResponse;
import cloudsufi.nextgen.tms.dto.UpdateUserRequestDTO;
import cloudsufi.nextgen.tms.dto.UserRequestDTO;
import cloudsufi.nextgen.tms.dto.UserSuggestionDTO;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing user-related operations in the Task Management System.
 * Provides endpoints to retrieve and create user details.
 *
 * @author Ansh Parnami
 */

@RestController
@RequestMapping("/api/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves a user based on ID, username, or email.
     * At least one parameter must be provided to perform the lookup.
     *
     * @param id       (Optional) The unique database ID of the user
     * @param username (Optional) The unique username of the user
     * @param email    (Optional) The unique email address of the user
     * @return ResponseEntity containing the {@link GetUserResponse} if found
     */
    @GetMapping
    public ResponseEntity<GetUserResponse> getUser(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        log.info("Received request to fetch user with params - ID: {}, Username: {}, Email: {}", id, username, email);

        return ResponseEntity.ok(userService.getUser(id, username, email));
    }

    /**
     * Creates a new user in the system.
     *
     * @param request DTO containing user creation details
     * @return ResponseEntity containing the created {@link UserEntity}
     */
    @PostMapping
    public ResponseEntity<UserEntity> addUser(@Valid @RequestBody UserRequestDTO request) {

        log.info("Received request to create user with username: {} and email: {}",
                request.getUsername(), request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.addUser(request));
    }

    /**
     * Searches for users whose usernames start with the provided string.
     * This endpoint is typically used for autocomplete or user suggestion features.
     *
     * Example:
     * /api/user/search?username=jo
     *
     * @param username The starting characters of the username used to search users.
     * The search is case-insensitive.
     *
     * @return A list of {@link UserSuggestionDTO} containing user IDs and usernames
     * that match the search criteria. Maximum 10 results are returned.
     * @author vishwasvaidya
     */


    @GetMapping("/search")
    public ResponseEntity<List<UserSuggestionDTO>> searchUsers(
            @RequestParam String username
    )
    {
        List<UserSuggestionDTO> users = userService
                .searchUsers(username, 0, 10)
                .getContent();


        return ResponseEntity.ok(users);
    }

    /**
     * Updates the logged-in user's username and/or phone number.
     *
     * @param request DTO containing fields to update (partial allowed)
     * @return ResponseEntity containing updated {@link GetUserResponse}
     *
     * @author Shubhanshu
     */
    @PutMapping
    public ResponseEntity<GetUserResponse> updateUser(
            @RequestBody UpdateUserRequestDTO request) {

        log.info("Received request to update logged-in user");

        GetUserResponse updatedUser = userService.updateUser(request);

        return ResponseEntity.ok(updatedUser);
    }

}