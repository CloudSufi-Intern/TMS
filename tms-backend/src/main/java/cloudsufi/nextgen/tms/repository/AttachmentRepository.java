package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository representing a Ticket's Attachment in the SQL Database
 * @author Ansh Parnami
 **/
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {
}
