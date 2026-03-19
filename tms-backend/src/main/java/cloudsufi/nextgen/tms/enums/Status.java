package cloudsufi.nextgen.tms.enums;

/**
 * Represents the primary lifecycle state of a support ticket.
 * As agents and users interact with the ticket, its status transitions linearly
 * or cyclically through these states from creation to final closure.
 *
 * @author Ansh Parnami
 */
public enum Status {
    OPEN,
    IN_PROGRESS,
    ON_HOLD,
    RESOLVED,
    CLOSED
}