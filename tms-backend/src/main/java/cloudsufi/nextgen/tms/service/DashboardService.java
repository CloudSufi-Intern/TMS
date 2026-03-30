package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.DashboardResponseDTO;
import cloudsufi.nextgen.tms.enums.Status;
import cloudsufi.nextgen.tms.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling dashboard analytics.
 *
 * This service aggregates ticket counts grouped by status
 * and maps them to dashboard response DTO.
 *
 * Current Implementation:
 * - Fetches global ticket counts (no user filtering)
 * - Maps ON_HOLD → Pending Approval (UI requirement)
 * - Ignores CLOSED tickets for now
 *
 * @author Shubhanshu
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final TicketRepository ticketRepository;

    /**
     * Fetches dashboard analytics data.
     *
     * Workflow:
     * 1. Fetch count of tickets for each required status
     * 2. Map enum values to dashboard fields
     * 3. Build and return response DTO
     *
     * @return DashboardResponseDTO containing aggregated counts
     */
    public DashboardResponseDTO getDashboardData() {

        log.info("Fetching dashboard analytics data");

        long openCount = ticketRepository.countByStatus(Status.OPEN);
        long inProgressCount = ticketRepository.countByStatus(Status.IN_PROGRESS);
        long pendingApprovalCount = ticketRepository.countByStatus(Status.ON_HOLD);
        long resolvedCount = ticketRepository.countByStatus(Status.RESOLVED);

        log.info("Dashboard counts fetched successfully");

        return DashboardResponseDTO.builder()
                .openTickets(openCount)
                .inProgressTickets(inProgressCount)
                .pendingApprovalTickets(pendingApprovalCount)
                .resolvedTickets(resolvedCount)
                .build();
    }
}