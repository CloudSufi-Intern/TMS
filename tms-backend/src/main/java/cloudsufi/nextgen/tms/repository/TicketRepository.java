package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.TicketEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository representing a Ticket in the SQL Database
 * @author Ansh Parnami
 **/
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
}
