package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.enums.FileType;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import cloudsufi.nextgen.tms.service.AttachmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for AttachmentController.
 *
 * @author Yashas Yadav
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttachmentService attachmentService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /api/attachments/{id}/download - Should return 200 with PDF content")
    void downloadAttachment_whenPDF_shouldReturnOkWithPdfContentType() throws Exception {
        byte[] fileBytes = "mock pdf content".getBytes();
        AttachmentEntity attachment = AttachmentEntity.builder()
                .id(1L)
                .fileName("report.pdf")
                .fileType(FileType.PDF)
                .file(fileBytes)
                .build();

        when(attachmentService.getAttachmentById(1L)).thenReturn(attachment);

        mockMvc.perform(get("/api/attachments/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"report.pdf\""))
                .andExpect(content().bytes(fileBytes));
    }

    @Test
    @DisplayName("GET /api/attachments/{id}/download - Should return 200 with image content type")
    void downloadAttachment_whenImage_shouldReturnOkWithImageContentType() throws Exception {
        byte[] imageBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        AttachmentEntity attachment = AttachmentEntity.builder()
                .id(2L)
                .fileName("screenshot.png")
                .fileType(FileType.IMAGE)
                .file(imageBytes)
                .build();

        when(attachmentService.getAttachmentById(2L)).thenReturn(attachment);

        mockMvc.perform(get("/api/attachments/2/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"screenshot.png\""));
    }

    @Test
    @DisplayName("GET /api/attachments/{id}/download - Should use fallback filename when fileName is blank")
    void downloadAttachment_whenFileNameBlank_shouldUseFallbackFilename() throws Exception {
        AttachmentEntity attachment = AttachmentEntity.builder()
                .id(5L)
                .fileName("")
                .fileType(FileType.PDF)
                .file("data".getBytes())
                .build();

        when(attachmentService.getAttachmentById(5L)).thenReturn(attachment);

        mockMvc.perform(get("/api/attachments/5/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"file_5\""));
    }

    @Test
    @DisplayName("GET /api/attachments/{id}/download - Should return 404 when attachment not found")
    void downloadAttachment_whenNotFound_shouldReturn404() throws Exception {
        when(attachmentService.getAttachmentById(999L))
                .thenThrow(new ResourceNotFoundException("Attachment not found with id: 999"));

        mockMvc.perform(get("/api/attachments/999/download"))
                .andExpect(status().isNotFound());
    }
}
