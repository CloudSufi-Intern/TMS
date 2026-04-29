package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.DashboardResponseDTO;
import cloudsufi.nextgen.tms.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Layer tests for Dashboard Controller.
 *
 * This test suite verifies:
 * - Endpoint accessibility
 * - Response structure
 * - Correct data returned from service
 *
 * @author Shubhanshu
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    /**
     * Verifies that dashboard API returns correct counts.
     */
    @Test
    @DisplayName("GET /api/dashboard - Should return dashboard analytics successfully")
    void getDashboard_whenCalled_shouldReturnDashboardData() throws Exception {

        DashboardResponseDTO response = DashboardResponseDTO.builder()
                .openTickets(2)
                .inProgressTickets(1)
                .pendingApprovalTickets(1)
                .resolvedTickets(1)
                .build();

        when(dashboardService.getDashboardData()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openTickets").value(2))
                .andExpect(jsonPath("$.inProgressTickets").value(1))
                .andExpect(jsonPath("$.pendingApprovalTickets").value(1))
                .andExpect(jsonPath("$.resolvedTickets").value(1));
    }

    /**
     * Verifies that API still works when all counts are zero.
     */
    @Test
    @DisplayName("GET /api/dashboard - Should return zero counts when no tickets exist")
    void getDashboard_whenNoTickets_shouldReturnZeroCounts() throws Exception {

        DashboardResponseDTO response = DashboardResponseDTO.builder()
                .openTickets(0)
                .inProgressTickets(0)
                .pendingApprovalTickets(0)
                .resolvedTickets(0)
                .build();

        when(dashboardService.getDashboardData()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openTickets").value(0))
                .andExpect(jsonPath("$.inProgressTickets").value(0))
                .andExpect(jsonPath("$.pendingApprovalTickets").value(0))
                .andExpect(jsonPath("$.resolvedTickets").value(0));
    }
}