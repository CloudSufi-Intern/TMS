package cloudsufi.nextgen.tms.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for returning login response details.
 * Contains the JWT token issued upon successful authentication.
 *
 * @author Yashas Yadav
 */
@Data
@Builder
public class LoginResponseDTO {

    private String token;

    private String tokenType;

    private String role;


}
