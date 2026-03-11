package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/*
 * Author: Yashas Yadav
 *
 * Description:
 * DTO used to receive sign-up requests from the client.
 * Prevents exposing the entity directly to the API layer.
 */

@Getter
@Setter
public class SignUpRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String phoneNo;

    private Role role;
}
