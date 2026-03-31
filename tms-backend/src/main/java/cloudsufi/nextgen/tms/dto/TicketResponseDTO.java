package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.ApprovalStatus;
import cloudsufi.nextgen.tms.enums.Priority;
import cloudsufi.nextgen.tms.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object returned for each ticket on the dashboard.
 * Exposes only the fields needed by the frontend — no internal entity
 * references or lazy-loaded associations are exposed directly.
 *
 * @author Yashas Yadav
 */
@Data
@Builder
public class TicketResponseDTO {

    private Long id;

    private String title;

    private String description;

    private Priority priority;

    private Status status;

    private LocalDateTime sla;

    /** Username of the user who raised the ticket. */
    private String createdBy;

    /** Username of the assigned user, or null if unassigned. */
    private String assignedTo;

    private boolean isApprovalRequired;

    /** Username of the approver, or null if not applicable. */
    private String approver;

    private ApprovalStatus approvalStatus;

    private LocalDateTime assignedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
