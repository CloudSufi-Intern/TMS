package cloudsufi.nextgen.tms.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO representing aggregated ticket counts for dashboard display.
 *
 * This DTO is used to send summarized analytics data to the frontend.
 * It contains counts of tickets grouped by status.
 *
 * Mapping:
 * - openTickets → Status.OPEN
 * - inProgressTickets → Status.IN_PROGRESS
 * - pendingApprovalTickets → Status.ON_HOLD
 * - resolvedTickets → Status.RESOLVED
 *
 * @author Shubhanshu
 */
@Data
@Builder
public class DashboardResponseDTO {

    private long openTickets;

    private long inProgressTickets;

    private long pendingApprovalTickets;

    private long resolvedTickets;
}