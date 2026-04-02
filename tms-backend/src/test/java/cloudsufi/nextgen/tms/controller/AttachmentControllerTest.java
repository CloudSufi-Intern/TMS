package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.enums.FileType;
import cloudsufi.nextgen.tms.exception.GlobalExceptionHandler;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.service.AttachmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AttachmentController
 *
 * This class uses Mockito + standalone MockMvc.
 * It avoids dependency issues with @MockBean / @WebMvcTest.
 *
 * Tests covered:
 * - Successful file download
 * - Attachment not found
 *
 * @author Smriti Bajpai
 */
@ExtendWith(MockitoExtension.class)
class AttachmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private AttachmentController attachmentController;

    /**
     * Setup MockMvc in standalone mode
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(attachmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Test: Successful download of attachment
     */
    @Test
    @DisplayName("GET /api/attachments/{id}/download - Success")
    void downloadAttachment_success() throws Exception {

        AttachmentEntity attachment = AttachmentEntity.builder()
                .id(1L)
                .file("hello".getBytes())
                .fileType(FileType.PDF)
                .build();

        when(attachmentService.getAttachmentById(1L)).thenReturn(attachment);


        mockMvc.perform(get("/api/attachments/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"file_1\""))
                .andExpect(content().bytes("hello".getBytes()))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    /**
     * Test: Attachment not found
     */
    @Test
    @DisplayName("GET /api/attachments/{id}/download - Not Found")
    void downloadAttachment_notFound() throws Exception {

        when(attachmentService.getAttachmentById(1L))
                .thenThrow(new ResourceNotFoundException("Attachment not found"));


        mockMvc.perform(get("/api/attachments/1/download"))
                .andExpect(status().isNotFound());
    }
}