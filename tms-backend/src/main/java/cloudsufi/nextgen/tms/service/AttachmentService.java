package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    @PreAuthorize("isAuthenticated()")
    public AttachmentEntity getAttachmentById(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + id));
    }
}
