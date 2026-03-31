package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.TicketHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


/**
 * Repository representing a Ticket's History in the SQL Database
 * @author Ansh Parnami
 **/
public interface TicketHistoryRepository extends JpaRepository<TicketHistoryEntity, Long> {
    List<TicketHistoryEntity> findByTicketId(Long ticketId);
}
