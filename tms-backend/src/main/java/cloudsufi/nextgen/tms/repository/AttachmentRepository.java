package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Collection;

/**
 * Repository representing a Ticket's Attachment in the SQL Database
 * @author Ansh Parnami
 **/
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {
    List<AttachmentEntity> findByTicketId(Long ticketId);
    long countByTicketId(Long ticketId);
}
