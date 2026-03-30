package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.ApprovalStatus;
import cloudsufi.nextgen.tms.enums.Priority;
import cloudsufi.nextgen.tms.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
/**
 * Data Transfer Object (DTO) representing the comprehensive details of a support ticket.
 * This class serves as the response payload for the "Get Ticket by ID" API, aggregating
 * core ticket information, assignment details, approval workflows, lightweight attachment
 * metadata, and a chronological history of audit logs.
 *
 * @author Ansh Parnami
 */
public class TicketDetailsResponse {


    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private LocalDateTime sla;

    private String createdBy;
    private String assignedTo;

    private boolean isApprovalRequired;
    private String approver;
    private ApprovalStatus approvalStatus;

    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    private List<AttachmentMetadata> attachments;
    private List<TicketHistory> history;

    @Data
    @Builder
    /**
     * Nested DTO representing lightweight metadata for a file attached to the ticket.
     * By returning only metadata (like file size derived from the database BLOB) instead
     * of the actual binary data, the API ensures high performance and reduced payload size.
     */
    public static class AttachmentMetadata {
        private Long id;
        private String fileType;
        private long fileSizeInBytes;
    }
    /**
     * Nested DTO representing a single event in the ticket's audit trail.
     * This provides a chronological view of actions taken on the ticket, including
     * what changed, who made the change, and when it occurred.
     */
    @Data
    @Builder
    public static class TicketHistory {
        private Long id;
        private String description;
        private String createdBy;
        private LocalDateTime createdAt;
    }
}