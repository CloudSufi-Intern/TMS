package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for ticket and comment attachments.
 */
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {

    List<AttachmentEntity> findByTicketIdAndCommentIsNullOrderByUploadedAtAsc(Long ticketId);

    List<AttachmentEntity> findByCommentIdOrderByUploadedAtAsc(Long commentId);

    long countByTicketIdAndCommentIsNull(Long ticketId);
}
