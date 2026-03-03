package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.Role;
import lombok.Builder;
import lombok.Data;



/**
 * Data Transfer Object (DTO) for returning user details.
 * This class is used to encapsulate user information sent back to the client,
 * ensuring that sensitive data (like passwords) is not exposed.
 * @author Ansh Parnami
 */
@Data
@Builder
public class GetUserResponse {
    /**
     * Unique identifier of the user.
     */
    private long id;

    private String username;

    private String email;

    private String phoneNo;

    /**
     * The organizational role assigned to the user.
     * @see cloudsufi.nextgen.tms.enums.Role
     */
    private Role role;




}
