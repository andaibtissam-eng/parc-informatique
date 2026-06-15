package com.parcinformatique.app.controller.api;

import com.parcinformatique.app.dto.common.ApiResponse;
import com.parcinformatique.app.dto.dashboard.DashboardSummaryDto;
import com.parcinformatique.app.security.UserPrincipal;
import com.parcinformatique.app.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('dashboard.read')")
    public ApiResponse<DashboardSummaryDto> summary(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok("Resume du tableau de bord", dashboardService.getSummary(principal.getId()));
    }
}
