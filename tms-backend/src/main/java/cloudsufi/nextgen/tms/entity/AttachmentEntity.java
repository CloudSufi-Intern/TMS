package cloudsufi.nextgen.tms.entity;

import cloudsufi.nextgen.tms.enums.FileType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a file attachment.
 *
 * An attachment is always linked to a ticket. It MAY additionally be linked to
 * a specific comment on that ticket. When {@code comment} is null, the
 * attachment is a top-level ticket attachment.
 *
 * Free-tier note: files are stored as {@code MEDIUMBLOB} in MySQL. This caps
 * each file at ~16 MB which is enforced both at the JPA layer and at the
 * Spring multipart layer ({@code spring.servlet.multipart.max-file-size}).
 */
@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "attachments_ticket_id_index", columnList = "ticket_id"),
        @Index(name = "attachments_comment_id_index", columnList = "comment_id")
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

    @Column(name = "file_name")
    private String fileName;

    @Lob
    @Column(name = "file", columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] file;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private TicketEntity ticket;

    /**
     * When non-null, this attachment is owned by a comment rather than the
     * ticket directly. Allows the same table to hold both ticket-level and
     * comment-level attachments without a schema duplication.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private CommentEntity comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false, updatable = false)
    private UserEntity uploadedBy;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
