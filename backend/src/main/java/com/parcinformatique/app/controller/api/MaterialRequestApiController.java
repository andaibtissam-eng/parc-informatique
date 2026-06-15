package com.parcinformatique.app.controller.api;

import com.parcinformatique.app.dto.common.ApiResponse;
import com.parcinformatique.app.dto.materialrequest.MaterialRequestCreateRequest;
import com.parcinformatique.app.dto.materialrequest.MaterialRequestDto;
import com.parcinformatique.app.dto.materialrequest.MaterialRequestReviewRequest;
import com.parcinformatique.app.security.UserPrincipal;
import com.parcinformatique.app.service.MaterialRequestService;
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
@RequestMapping("/api/material-requests")
@RequiredArgsConstructor
public class MaterialRequestApiController {

    private final MaterialRequestService materialRequestService;

    @GetMapping
    @PreAuthorize("hasAuthority('assignments.manage') or hasAuthority('assignments.read.own')")
    public ApiResponse<List<MaterialRequestDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok("Demandes de materiel", materialRequestService.list(principal.getId()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('assignments.read.own')")
    public ApiResponse<MaterialRequestDto> create(
        @Valid @RequestBody MaterialRequestCreateRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Demande de materiel creee",
            materialRequestService.create(request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PatchMapping("/{requestId}/review")
    @PreAuthorize("hasAuthority('assignments.manage')")
    public ApiResponse<MaterialRequestDto> review(
        @PathVariable UUID requestId,
        @Valid @RequestBody MaterialRequestReviewRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Demande traitee",
            materialRequestService.review(requestId, request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }
}
