package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for ticket comments.
 * @author Priyanshu Gupta
 */
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
    long countByTicketId(Long ticketId);
}