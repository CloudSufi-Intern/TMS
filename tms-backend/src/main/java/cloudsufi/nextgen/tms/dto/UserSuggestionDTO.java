package cloudsufi.nextgen.tms.dto;
/**
 * Data Transfer Object used for returning lightweight
 * user suggestion information.
 *
 * This DTO is typically used in search or autocomplete
 * operations where only minimal user information is required.
 * @author vishwasvaidya
 */
public class UserSuggestionDTO {
    private Long id;
    private String username;

    /**
     * Constructs a new UserSuggestionDTO.
     *
     * @param id The unique identifier of the user.
     * @param username The username of the user.
     */

    public UserSuggestionDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
}
