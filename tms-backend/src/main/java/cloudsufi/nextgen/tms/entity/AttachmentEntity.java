package cloudsufi.nextgen.tms.entity;

import cloudsufi.nextgen.tms.enums.FileType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a file attachment linked to a support ticket.
 * This class maps to the {@code attachments} table and stores the actual
 * binary data of the file (BLOB) along with metadata such as who uploaded it
 * and the specific ticket it belongs to.
 *
 * @author Ansh Parnami
 */
@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "attachments_ticket_id_index", columnList = "ticket_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "file", columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] file;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private TicketEntity ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false, updatable = false)
    private UserEntity uploadedBy;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
