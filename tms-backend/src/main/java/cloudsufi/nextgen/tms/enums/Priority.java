package cloudsufi.nextgen.tms.enums;


/**
 * Defines the severity or urgency level of a ticket.
 * The priority level is typically used to determine the Service Level Agreement (SLA)
 * deadlines, dictate routing logic to specific queues, and help support agents
 * decide which tickets to address first.
 *
 * @author Ansh Parnami
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
