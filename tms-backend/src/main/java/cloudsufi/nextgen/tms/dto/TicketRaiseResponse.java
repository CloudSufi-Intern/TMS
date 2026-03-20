package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.Status;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for sending the response back to the client
 * after a successful ticket creation request.
 * This class encapsulates the confirmation details, including the newly
 * generated ticket ID and its initial status.
 * @author Ansh Parnami
 */
@Data
@Builder
public class TicketRaiseResponse {
    Long ticketId;
    Status status;
    String message;
}
