package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

/**
 * DTO for handling incoming requests to raise a new support ticket.
 * * [Ticket Update]: Enforced strict validation rules (@NotBlank, @NotNull)
 * on mandatory fields to ensure no empty records are saved to the database.
 * Added private access modifiers to enforce proper data encapsulation.
 * * @author Priyanshu Gupta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRaiseRequest {
    @NotBlank(message = "Title is required and cannot be empty.")
     private String title;


    @NotBlank(message = "Description  is required and cannot be empty")
    private String description;

    @NotNull(message = "Priority is required.")
    private Priority priority;

    @Builder.Default
    private List<MultipartFile> attachments = new ArrayList<>();
}
