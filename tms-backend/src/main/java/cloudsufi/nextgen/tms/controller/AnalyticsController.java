package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.AnalyticsResponseDTO;
import cloudsufi.nextgen.tms.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<AnalyticsResponseDTO> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    }
}
