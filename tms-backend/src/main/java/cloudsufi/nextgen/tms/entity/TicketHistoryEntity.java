package cloudsufi.nextgen.tms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing the audit log and history of a support ticket.
 * This class maps to the {@code tickets_history} table. It is used to track
 * every major lifecycle event, status change, or assignment update that occurs
 * on a ticket, providing a chronological timeline of actions.
 *
 * @author Ansh Parnami
 */
@Entity
@Table(name = "tickets_history", indexes = {
        @Index(name = "tickets_history_ticket_id_index", columnList = "ticket_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
