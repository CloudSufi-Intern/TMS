package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ticket comments.
 */
@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    List<CommentEntity> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    List<CommentEntity> findByTicketIdAndCreatedByUsernameOrderByCreatedAtAsc(Long ticketId, String username);

    long countByTicketId(Long ticketId);
}
