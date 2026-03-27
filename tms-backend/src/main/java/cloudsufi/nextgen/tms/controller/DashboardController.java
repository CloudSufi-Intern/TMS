package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.DashboardResponseDTO;
import cloudsufi.nextgen.tms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for exposing dashboard analytics API.
 *
 * This controller provides an endpoint to fetch ticket counts
 * grouped by status for dashboard display.
 *
 * Endpoint:
 * GET /api/dashboard
 *
 * @author Shubhanshu
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Fetch dashboard analytics data.
     *
     * @return ResponseEntity containing DashboardResponseDTO
     */
    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboardData() {

        return ResponseEntity.ok(dashboardService.getDashboardData());
    }
}