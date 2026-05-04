package cloudsufi.nextgen.tms.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Lightweight projection of an attachment — file BLOB stripped out.
 * Used everywhere the UI needs to list attachments without paying for the bytes.
 */
@Data
@Builder
public class AttachmentMetadataDTO {

    private Long id;
    private String fileName;
    private String fileType;
    private long fileSizeInBytes;
}
