package cloudsufi.nextgen.tms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/*
 * Author: Yashas Yadav
 *
 * Description:
 * DTO used to receive login requests from the client.
 * Prevents exposing the entity directly to the API layer.
 */

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
