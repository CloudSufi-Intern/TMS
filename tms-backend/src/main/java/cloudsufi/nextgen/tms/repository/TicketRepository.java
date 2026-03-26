package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.TicketEntity;
import cloudsufi.nextgen.tms.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository representing a Ticket in the SQL Database
 * @author Ansh Parnami
 **/
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    /**
     * Fetches all tickets where the given user is either the creator (raiser)
     * or the assignee, ordered by creation date descending (newest first).
     *
     * Uses JOIN FETCH on createdBy and LEFT JOIN FETCH on assignedTo and approver
     * to load all required associations in a single query, avoiding N+1 problems
     * on the dashboard page load.
     *
     * @param user The authenticated user entity.
     * @return List of tickets relevant to this user for the dashboard.
     * @author Yashas Yadav
     */
    @Query("""
            SELECT t FROM TicketEntity t
            JOIN FETCH t.createdBy cb
            LEFT JOIN FETCH t.assignedTo a
            LEFT JOIN FETCH t.approver ap
            WHERE t.createdBy = :user OR t.assignedTo = :user
            ORDER BY t.createdAt DESC
            """)
    List<TicketEntity> findAllByCreatedByOrAssignedTo(@Param("user") UserEntity user);
}
