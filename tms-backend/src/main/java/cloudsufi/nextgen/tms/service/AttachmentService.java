package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling business logic
 * related to attachments.
 *
 * This class interacts with the AttachmentRepository to:
 * - Fetch attachment data from the database
 * - Perform validations and error handling
 *
 * It acts as a bridge between the controller layer and the data layer.
 *
 * @author Smriti Bajpai
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;

    public AttachmentEntity getAttachmentById(Long id){
        log.info("Fetching attachment from DB with ID: {}", id);
        return attachmentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Attachment not found with id!"+id));
    }
}
