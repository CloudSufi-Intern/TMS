package cloudsufi.nextgen.tms.enums;

/**
 * Defines the possible states of the approval workflow for a given ticket.
 * Not all tickets require managerial or systemic approval. This enum tracks
 * where a ticket currently sits in that specific sub-process.
 *
 * @author Ansh Parnami
 */
public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    NOT_REQUIRED
}
