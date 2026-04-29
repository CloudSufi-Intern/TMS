package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.TicketEntity;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.Status;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Ticket repository.
 */
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    @Query("""
            SELECT t FROM TicketEntity t
            JOIN FETCH t.createdBy cb
            LEFT JOIN FETCH t.assignedTo a
            LEFT JOIN FETCH t.approver ap
            WHERE t.createdBy = :user OR t.assignedTo = :user
            """)
    List<TicketEntity> findAllByCreatedByOrAssignedTo(@Param("user") UserEntity user, Sort sort);

    @Query("""
            SELECT t FROM TicketEntity t
            JOIN FETCH t.createdBy cb
            LEFT JOIN FETCH t.assignedTo a
            LEFT JOIN FETCH t.approver ap
            """)
    List<TicketEntity> findAllWithAssociations(Sort sort);

    long countByStatus(Status status);
}
