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

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) {
        AttachmentEntity attachment = attachmentService.getAttachmentById(id);
        String displayName = (attachment.getFileName() != null && !attachment.getFileName().isBlank())
                ? attachment.getFileName()
                : "file_" + id;

        return ResponseEntity.ok()
                .contentType(getMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + displayName.replace("\"", "") + "\"")
                .body(attachment.getFile());
    }

    private MediaType getMediaType(FileType fileType) {
        return switch (fileType) {
            case IMAGE -> MediaType.IMAGE_PNG;
            case PDF -> MediaType.APPLICATION_PDF;
        };
    }
}
