package cloudsufi.nextgen.tms.dto;

import cloudsufi.nextgen.tms.enums.ApprovalStatus;
import cloudsufi.nextgen.tms.enums.Priority;
import cloudsufi.nextgen.tms.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprehensive ticket details payload, including attachment metadata
 * and chronological audit history.
 */
@Data
@Builder
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

    /**
     * Lightweight metadata for a file attached to the ticket. fileName is
     * surfaced so the frontend can display the original upload name.
     */
    @Data
    @Builder
    public static class AttachmentMetadata {
        private Long id;
        private String fileName;
        private String fileType;
        private long fileSizeInBytes;
    }

    /** Single audit-log event. */
    @Data
    @Builder
    public static class TicketHistory {
        private Long id;
        private String description;
        private String createdBy;
        private LocalDateTime createdAt;
    }
}
