package cloudsufi.nextgen.tms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comment payload returned to the frontend.
 * Now includes the list of attachments associated with this comment.
 */
@Data
@Builder
public class CommentResponseDTO {

    private Long id;
    private String content;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<AttachmentMetadataDTO> attachments;
}
