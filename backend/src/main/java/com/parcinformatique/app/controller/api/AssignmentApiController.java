package com.parcinformatique.app.controller.api;

import com.parcinformatique.app.dto.assignment.AssignmentCreateRequest;
import com.parcinformatique.app.dto.assignment.AssignmentDetailDto;
import com.parcinformatique.app.dto.assignment.AssignmentReturnRequest;
import com.parcinformatique.app.dto.assignment.AssignmentSummaryDto;
import com.parcinformatique.app.dto.common.ApiResponse;
import com.parcinformatique.app.dto.common.PaginatedResponse;
import com.parcinformatique.app.security.UserPrincipal;
import com.parcinformatique.app.service.AssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentApiController {

    private final AssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('assignments.manage') or hasAuthority('assignments.read.own')")
    public ApiResponse<PaginatedResponse<AssignmentSummaryDto>> listAssignments(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok("Liste des affectations", assignmentService.listAssignments(principal.getId(), page, size));
    }

    @GetMapping("/{assignmentId}")
    @PreAuthorize("hasAuthority('assignments.manage') or hasAuthority('assignments.read.own')")
    public ApiResponse<AssignmentDetailDto> getAssignment(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID assignmentId
    ) {
        return ApiResponse.ok("Detail affectation", assignmentService.getAssignment(principal.getId(), assignmentId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('assignments.manage')")
    public ApiResponse<AssignmentDetailDto> createAssignment(
        @Valid @RequestBody AssignmentCreateRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Affectation creee",
            assignmentService.createAssignment(request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PatchMapping("/{assignmentId}/approve")
    @PreAuthorize("hasAuthority('assignments.manage')")
    public ApiResponse<AssignmentDetailDto> approveAssignment(
        @PathVariable UUID assignmentId,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Affectation validee",
            assignmentService.approveAssignment(assignmentId, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PostMapping("/{assignmentId}/return")
    @PreAuthorize("hasAuthority('assignments.manage') or hasAuthority('maintenances.manage')")
    public ApiResponse<AssignmentDetailDto> returnAssignment(
        @PathVariable UUID assignmentId,
        @Valid @RequestBody AssignmentReturnRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Retour materiel enregistre",
            assignmentService.returnAssignment(assignmentId, request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }
}
