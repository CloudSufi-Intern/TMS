package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.Priority;
import cloudsufi.nextgen.tms.enums.Status;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for handling partial ticket updates via PATCH requests.
 * <p>
 * This class encapsulates the specific fields that a client or agent is permitted
 * to modify after a ticket has been created. In a standard RESTful PATCH operation,
 * all fields are strictly optional.
 * </p>
 * <p>
 * If a field is omitted from the incoming JSON payload (resulting in a {@code null}
 * value within this DTO), the underlying service logic will ignore it and preserve
 * the ticket's existing value in the database.
 * </p>
 *
 * @author Ansh Parnami
 */
@Data
public class TicketUpdatePatchRequest {
    private Status status;
    private Priority priority;
    private String assigneeEmail;
}