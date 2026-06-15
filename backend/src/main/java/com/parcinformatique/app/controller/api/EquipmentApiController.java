package com.parcinformatique.app.controller.api;

import com.parcinformatique.app.dto.common.ApiResponse;
import com.parcinformatique.app.dto.common.PaginatedResponse;
import com.parcinformatique.app.dto.equipment.EquipmentDetailDto;
import com.parcinformatique.app.dto.equipment.EquipmentSummaryDto;
import com.parcinformatique.app.dto.equipment.EquipmentUpsertRequest;
import com.parcinformatique.app.security.UserPrincipal;
import com.parcinformatique.app.service.EquipmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
public class EquipmentApiController {

    private final EquipmentService equipmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('equipments.read') or hasAuthority('equipments.manage')")
    public ApiResponse<PaginatedResponse<EquipmentSummaryDto>> listEquipments(
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok("Liste des equipements", equipmentService.listEquipments(search, page, size));
    }

    @GetMapping("/{equipmentId}")
    @PreAuthorize("hasAuthority('equipments.read') or hasAuthority('equipments.manage')")
    public ApiResponse<EquipmentDetailDto> getEquipment(@PathVariable UUID equipmentId) {
        return ApiResponse.ok("Detail equipement", equipmentService.getEquipment(equipmentId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('equipments.manage')")
    public ApiResponse<EquipmentDetailDto> createEquipment(
        @Valid @RequestBody EquipmentUpsertRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Equipement cree",
            equipmentService.createEquipment(request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PutMapping("/{equipmentId}")
    @PreAuthorize("hasAuthority('equipments.manage')")
    public ApiResponse<EquipmentDetailDto> updateEquipment(
        @PathVariable UUID equipmentId,
        @Valid @RequestBody EquipmentUpsertRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Equipement mis a jour",
            equipmentService.updateEquipment(equipmentId, request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PostMapping(path = "/{equipmentId}/image", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('equipments.manage')")
    public ApiResponse<EquipmentDetailDto> uploadImage(
        @PathVariable UUID equipmentId,
        @RequestPart("file") MultipartFile file,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Photo equipement televersee",
            equipmentService.uploadEquipmentImage(equipmentId, file, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PostMapping(path = "/{equipmentId}/document", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('equipments.manage')")
    public ApiResponse<EquipmentDetailDto> uploadDocument(
        @PathVariable UUID equipmentId,
        @RequestPart("file") MultipartFile file,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Document equipement televerse",
            equipmentService.uploadEquipmentDocument(equipmentId, file, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }
}
