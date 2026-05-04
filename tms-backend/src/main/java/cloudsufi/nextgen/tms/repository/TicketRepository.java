package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.TicketEntity;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.Status;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    long countByStatusAndCreatedByOrStatusAndAssignedTo(Status s1, UserEntity u1, Status s2, UserEntity u2);

    // --- Global analytics queries (IT) ---

    @Query("SELECT t.priority, COUNT(t) FROM TicketEntity t GROUP BY t.priority")
    List<Object[]> countGroupByPriority();

    @Query("SELECT t.assignedTo.username, COUNT(t) FROM TicketEntity t WHERE t.assignedTo IS NOT NULL GROUP BY t.assignedTo")
    List<Object[]> countGroupByAssignee();

    @Query("SELECT t FROM TicketEntity t WHERE t.createdAt >= :since")
    List<TicketEntity> findCreatedSince(@Param("since") LocalDateTime since);

    // --- User-scoped analytics queries (non-IT) ---

    @Query("SELECT t.priority, COUNT(t) FROM TicketEntity t WHERE t.createdBy = :user OR t.assignedTo = :user GROUP BY t.priority")
    List<Object[]> countGroupByPriorityForUser(@Param("user") UserEntity user);

    @Query("SELECT t.assignedTo.username, COUNT(t) FROM TicketEntity t WHERE t.assignedTo IS NOT NULL AND (t.createdBy = :user OR t.assignedTo = :user) GROUP BY t.assignedTo")
    List<Object[]> countGroupByAssigneeForUser(@Param("user") UserEntity user);

    @Query("SELECT t FROM TicketEntity t WHERE t.createdAt >= :since AND (t.createdBy = :user OR t.assignedTo = :user)")
    List<TicketEntity> findCreatedSinceForUser(@Param("since") LocalDateTime since, @Param("user") UserEntity user);

    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.status = :status AND (t.createdBy = :user OR t.assignedTo = :user)")
    long countByStatusForUser(@Param("status") Status status, @Param("user") UserEntity user);
}
