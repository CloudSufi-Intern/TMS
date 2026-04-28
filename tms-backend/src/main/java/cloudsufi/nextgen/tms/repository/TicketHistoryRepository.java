package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.TicketHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Repository representing a Ticket's History in the SQL Database
 * @author Ansh Parnami
 **/
@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistoryEntity, Long> {
    List<TicketHistoryEntity> findByTicket_IdOrderByCreatedAtDesc(Long ticketId);
}
