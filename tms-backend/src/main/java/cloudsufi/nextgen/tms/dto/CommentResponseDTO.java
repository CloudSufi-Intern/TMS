package cloudsufi.nextgen.tms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for returning comment data to the frontend.
 * @author Priyanshu Gupta
 */
@Data
@Builder
public class CommentResponseDTO {

    private Long id;
    private String content;
    private String createdBy;
    private LocalDateTime createdAt;
}