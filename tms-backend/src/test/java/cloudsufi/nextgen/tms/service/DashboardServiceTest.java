package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.DashboardResponseDTO;
import cloudsufi.nextgen.tms.enums.Status;
import cloudsufi.nextgen.tms.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Service Layer tests for DashboardService.
 *
 * This test suite verifies:
 * - Correct aggregation of ticket counts
 * - Proper mapping of status enums
 *
 * @author Shubhanshu
 */
@SpringBootTest
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @MockitoBean
    private TicketRepository ticketRepository;

    /**
     * Verifies that service correctly aggregates ticket counts.
     */
    @Test
    @DisplayName("Service - Should return correct dashboard counts")
    void getDashboardData_shouldReturnCorrectCounts() {

        when(ticketRepository.countByStatus(Status.OPEN)).thenReturn(5L);
        when(ticketRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(3L);
        when(ticketRepository.countByStatus(Status.ON_HOLD)).thenReturn(2L);
        when(ticketRepository.countByStatus(Status.RESOLVED)).thenReturn(4L);

        DashboardResponseDTO response = dashboardService.getDashboardData();

        assertNotNull(response);
        assertEquals(5, response.getOpenTickets());
        assertEquals(3, response.getInProgressTickets());
        assertEquals(2, response.getPendingApprovalTickets());
        assertEquals(4, response.getResolvedTickets());

        verify(ticketRepository).countByStatus(Status.OPEN);
        verify(ticketRepository).countByStatus(Status.IN_PROGRESS);
        verify(ticketRepository).countByStatus(Status.ON_HOLD);
        verify(ticketRepository).countByStatus(Status.RESOLVED);
    }

    /**
     * Verifies that service handles zero counts correctly.
     */
    @Test
    @DisplayName("Service - Should handle zero ticket counts")
    void getDashboardData_whenNoTickets_shouldReturnZeroCounts() {

        when(ticketRepository.countByStatus(any())).thenReturn(0L);

        DashboardResponseDTO response = dashboardService.getDashboardData();

        assertNotNull(response);
        assertEquals(0, response.getOpenTickets());
        assertEquals(0, response.getInProgressTickets());
        assertEquals(0, response.getPendingApprovalTickets());
        assertEquals(0, response.getResolvedTickets());
    }

    /**
     * Verifies mapping logic (ON_HOLD → Pending Approval).
     */
    @Test
    @DisplayName("Service - Should map ON_HOLD to Pending Approval correctly")
    void getDashboardData_shouldMapOnHoldCorrectly() {

        when(ticketRepository.countByStatus(Status.ON_HOLD)).thenReturn(7L);

        DashboardResponseDTO response = dashboardService.getDashboardData();

        assertEquals(7, response.getPendingApprovalTickets());
    }
}