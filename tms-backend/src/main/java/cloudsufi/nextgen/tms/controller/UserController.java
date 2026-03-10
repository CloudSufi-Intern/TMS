package cloudsufi.nextgen.tms.controller;


import cloudsufi.nextgen.tms.dto.UserSuggestionDTO;
import cloudsufi.nextgen.tms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cloudsufi.nextgen.tms.dto.GetUserResponse;

import java.util.List;

/**
 * REST Controller for managing user-related operations in the Task Management System.
 * Provides endpoints to retrieve user details based on various identifiers.
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
     * @return ResponseEntity containing the {@link GetUserResponse} if found,
     * or a Bad Request message if no parameters are provided.
     */
    @GetMapping
    ResponseEntity<?> getUser(@RequestParam(required = false) Long id ,@RequestParam(required = false) String username,@RequestParam(required = false) String email){
        log.info("Received request to fetch user with params - ID: {}, Username: {}, Email: {}", id, username, email);


            return new ResponseEntity<>(userService.getUser(id, username, email), HttpStatus.OK);

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
    public List<UserSuggestionDTO> searchUsers(
            @RequestParam String username
    )
    {

        return userService
                .searchUsers(username, 0, 10)
                .getContent();
    }
}
