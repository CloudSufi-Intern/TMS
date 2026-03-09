package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.UserSuggestionDTO;
import cloudsufi.nextgen.tms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Searches for users whose usernames start with the provided string.
     * This endpoint is typically used for autocomplete or user suggestion features.
     *
     * Example:
     * /api/user/search?username=jo
     *
     * @param username The starting characters of the username used to search users.
     *                 The search is case-insensitive.
     *
     * @return A list of {@link UserSuggestionDTO} containing user IDs and usernames
     *         that match the search criteria. Maximum 10 results are returned.
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
