package cloudsufi.nextgen.tms.controller;


import cloudsufi.nextgen.tms.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cloudsufi.nextgen.tms.dto.GetUserResponse;

/**
 * REST Controller for managing user-related operations in the Task Management System.
 * Provides endpoints to retrieve user details based on various identifiers.
 * @author Ansh Parnami
 */
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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
        boolean hasId = id != null;
        boolean hasUsername = username != null && !username.isBlank();
        boolean hasEmail = email != null && !email.isBlank();

        if (!hasId && !hasUsername && !hasEmail) {
            log.warn("GET /api/user called without any search parameters");
           return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                   .body("At least one search parameter (id, username, or email) must be provided.");
       }

            return new ResponseEntity<>(userService.getUser(id, username, email), HttpStatus.OK);

    }


    }

