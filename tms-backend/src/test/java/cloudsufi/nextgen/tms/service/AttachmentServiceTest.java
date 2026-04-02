package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.AttachmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
/**
 * Unit tests for AttachmentService
 *
 * Tests covered:
 * - Successful get attachment by id
 * - Attachment not found by id
 *
 * @author Smriti Bajpai
 */
class AttachmentServiceTest{

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    public AttachmentServiceTest(){
        MockitoAnnotations.initMocks(this);
    }
    /**
     * Test: Attachment found successfully
     */
    @Test
    @DisplayName("Get attachment by ID -success")
    void getAttachmentByIdSuccess(){
        AttachmentEntity attachment = AttachmentEntity.builder()
                .id(1L)
                .file("hello".getBytes())
                .build();
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));
          AttachmentEntity result = attachmentService.getAttachmentById(1L);
         assertNotNull(result);
         assertEquals(1L,result.getId());
    }

    /**
     * Test: Attachment not found
     */
    @Test
    @DisplayName("Get attachment by ID - Not Found")
    void getAttachmentById_notFound() {

        when(attachmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            attachmentService.getAttachmentById(1L);
        });
    }

}