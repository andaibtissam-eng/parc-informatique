package com.parcinformatique.app.controller.api;

import com.parcinformatique.app.dto.common.ApiResponse;
import com.parcinformatique.app.dto.maintenance.MaintenanceCardDto;
import com.parcinformatique.app.dto.maintenance.MaintenanceListDto;
import com.parcinformatique.app.dto.maintenance.MaintenanceStatusUpdateRequest;
import com.parcinformatique.app.dto.maintenance.MaintenanceUpsertRequest;
import com.parcinformatique.app.security.UserPrincipal;
import com.parcinformatique.app.service.MaintenanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
public class MaintenanceApiController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('maintenances.read') or hasAuthority('maintenances.manage')")
    public ApiResponse<List<MaintenanceListDto>> list() {
        return ApiResponse.ok("Liste maintenances", maintenanceService.listMaintenances());
    }

    @GetMapping("/board")
    @PreAuthorize("hasAuthority('maintenances.read') or hasAuthority('maintenances.manage')")
    public ApiResponse<List<MaintenanceCardDto>> board() {
        return ApiResponse.ok("Tableau maintenance", maintenanceService.getBoard());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('maintenances.manage') or hasAuthority('maintenances.request')")
    public ApiResponse<MaintenanceCardDto> create(
        @Valid @RequestBody MaintenanceUpsertRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Maintenance creee",
            maintenanceService.createMaintenance(request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PatchMapping("/{maintenanceId}/status")
    @PreAuthorize("hasAuthority('maintenances.manage')")
    public ApiResponse<MaintenanceCardDto> updateStatus(
        @PathVariable UUID maintenanceId,
        @Valid @RequestBody MaintenanceStatusUpdateRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Statut maintenance mis a jour",
            maintenanceService.updateStatus(maintenanceId, request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }
}
