package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.enums.FileType;
import cloudsufi.nextgen.tms.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * REST Controller responsible for handling all attachment-related APIs.
 *
 * This controller provides endpoints to:
 * - Download attachment files by ID
 *
 * It interacts with the AttachmentService to fetch attachment data
 * from the database and returns it as a downloadable response.
 *
 * @author Smriti Bajpai
 */
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) {

        log.info("Download request received for attachment ID: {}", id);

        AttachmentEntity attachment = attachmentService.getAttachmentById(id);

        log.info("Attachment found. Preparing download for ID: {}", id);

        return ResponseEntity.ok()
                .contentType(getMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"file_" + id + "\"")
                .body(attachment.getFile());
    }

    private MediaType getMediaType(FileType fileType) {
        return switch (fileType) {
            case IMAGE -> MediaType.IMAGE_PNG;
            case PDF -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}