package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository representing a Ticket's Attachment in the SQL Database
 * @author Ansh Parnami
 **/
@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {
    List<AttachmentEntity> findByTicket_IdOrderByUploadedAtDesc(Long ticketId);
    long countByTicket_Id(Long ticketId);
}
