package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.Role;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for returning login response details.
 * Contains the JWT token issued upon successful authentication.
 *
 * @author Yashas Yadav
 */
/**
 * [update]:Added additional required fields as per ticket #53
 *
 * @author Ansh Parnami
 */
@Data
@Builder
public class LoginResponseDTO {

    private String token;

    private String tokenType;

<<<<<<< HEAD
    private Long id;

    private String username;

    private String email;

    private String phoneNo;

    /**
     * The organizational role assigned to the user.
     * @see cloudsufi.nextgen.tms.enums.Role
     */
    private Role role;
>>>>>>> origin/develop

}
