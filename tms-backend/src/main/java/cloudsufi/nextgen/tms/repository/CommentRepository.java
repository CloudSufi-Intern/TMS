package cloudsufi.nextgen.tms.repository;

import cloudsufi.nextgen.tms.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ticket comments.
 * @author Priyanshu Gupta
 */
@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByTicket_IdOrderByCreatedAtDesc(Long ticketId);
    long countByTicket_Id(Long ticketId);
}
