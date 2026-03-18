package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for returning sign-up response details.
 * Ensures sensitive data (like passwords) is not exposed to the client.
 *
 * @author Yashas Yadav
 */
@Data
@Builder
public class SignUpResponseDTO {

    private Long id;

    private String username;

    private String email;

    private String phoneNo;

    /**
     * The organizational role assigned to the user.
     * @see cloudsufi.nextgen.tms.enums.Role
     */
    private Role role;

    private LocalDateTime createdAt;
}
