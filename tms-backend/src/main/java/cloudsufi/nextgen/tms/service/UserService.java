package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.UserSuggestionDTO;
import cloudsufi.nextgen.tms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

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
     *         objects representing matching users.
     */

    public Page<UserSuggestionDTO> searchUsers(String username, int page, int size) {
        if (username == null || username.trim().length() < 2) {
            throw new IllegalArgumentException("Username must contain at least 2 characters");
        }
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.searchUsers(username, pageable);
    }

}
