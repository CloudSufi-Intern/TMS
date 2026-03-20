package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.TicketHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository representing a Ticket's History in the SQL Database
 * @author Ansh Parnami
 **/
public interface TicketHistoryRepository extends JpaRepository<TicketHistoryEntity, Long> {
}
