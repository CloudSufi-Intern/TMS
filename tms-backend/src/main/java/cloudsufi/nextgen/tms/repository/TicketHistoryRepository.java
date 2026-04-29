package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.TicketHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ticket history (audit log) entries.
 */
@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistoryEntity, Long> {

    List<TicketHistoryEntity> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    List<TicketHistoryEntity> findByTicketId(Long ticketId);
}
