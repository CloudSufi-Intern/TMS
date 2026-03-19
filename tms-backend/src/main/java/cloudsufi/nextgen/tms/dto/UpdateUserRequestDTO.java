package cloudsufi.nextgen.tms.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating user details.
 *
 * Fields are optional to support partial updates.
 * At least one field must be provided.
 *
 * @author Shubhanshu
 */
@Getter
@Setter
public class UpdateUserRequestDTO {

    /**
     * New username for the user (optional).
     */
    private String username;

    /**
     * Updated phone number for the user (optional).
     */
    private String phoneNo;
}