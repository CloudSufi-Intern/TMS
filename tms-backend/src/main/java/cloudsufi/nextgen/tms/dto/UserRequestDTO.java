package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/*
 * Author: Smriti Bajpai
 * Description:
 * DTO used to receive user creation requests from the client.
 * Prevents exposing the entity directly to the API layer.
 */

@Getter
@Setter
public class UserRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    private String phoneNo;

    private String password;

    private Role role;
}