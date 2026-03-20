package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.Priority;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for handling incoming requests to raise a new support ticket.
 * This class encapsulates the raw data provided by the client (e.g., frontend application)
 * required to initialize a ticket in the system. The payload is automatically deserialized
 * from JSON into this object by Spring's Jackson `ObjectMapper`.
 *
 * @author Ansh Parnami
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRaiseRequest {
    String title;
    String description;
    Priority priority;

    @Builder.Default
    private List<MultipartFile> attachments = new ArrayList<>();
}
