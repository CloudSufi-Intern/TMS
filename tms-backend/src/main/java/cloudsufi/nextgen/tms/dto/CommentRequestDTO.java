package cloudsufi.nextgen.tms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for incoming comment creation requests.
 * @author Priyanshu Gupta
 */
@Data
public class CommentRequestDTO {

    @NotBlank(message = "Comment content cannot be empty.")
    private String content;
}