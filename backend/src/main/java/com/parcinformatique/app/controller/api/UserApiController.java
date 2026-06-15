package com.parcinformatique.app.controller.api;

import com.parcinformatique.app.dto.common.ApiResponse;
import com.parcinformatique.app.dto.common.PaginatedResponse;
import com.parcinformatique.app.dto.user.UserAdminOverviewDto;
import com.parcinformatique.app.dto.user.UserDetailDto;
import com.parcinformatique.app.dto.user.UserRoleUpdateRequest;
import com.parcinformatique.app.dto.user.UserSummaryDto;
import com.parcinformatique.app.dto.user.UserUpsertRequest;
import com.parcinformatique.app.enums.UserStatus;
import com.parcinformatique.app.security.UserPrincipal;
import com.parcinformatique.app.service.UserService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<PaginatedResponse<UserSummaryDto>> listUsers(
        @RequestParam(defaultValue = "") String search,
        @RequestParam(required = false) UserStatus status,
        @RequestParam(required = false) String role,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok("Liste des utilisateurs", userService.listUsers(search, status, role, page, size));
    }

    @GetMapping("/admin-overview")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserAdminOverviewDto> adminOverview() {
        return ApiResponse.ok("Vue d'ensemble administration utilisateurs", userService.getAdminOverview());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserDetailDto> getUser(@PathVariable UUID userId) {
        return ApiResponse.ok("Detail utilisateur", userService.getUser(userId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserDetailDto> createUser(
        @Valid @RequestBody UserUpsertRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Utilisateur cree",
            userService.createUser(request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserDetailDto> updateUser(
        @PathVariable UUID userId,
        @Valid @RequestBody UserUpsertRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Utilisateur mis a jour",
            userService.updateUser(userId, request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PatchMapping("/{userId}/approve")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserSummaryDto> approveUser(
        @PathVariable UUID userId,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Compte approuve",
            userService.approveUser(userId, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PatchMapping("/{userId}/reject")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserSummaryDto> rejectUser(
        @PathVariable UUID userId,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Compte rejete",
            userService.rejectUser(userId, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PatchMapping("/{userId}/suspend")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserSummaryDto> suspendUser(
        @PathVariable UUID userId,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Compte suspendu",
            userService.suspendUser(userId, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserSummaryDto> activateUser(
        @PathVariable UUID userId,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Compte active",
            userService.activateUser(userId, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserSummaryDto> deactivateUser(
        @PathVariable UUID userId,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Compte desactive",
            userService.deactivateUser(userId, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }

    @PatchMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserSummaryDto> updateRoles(
        @PathVariable UUID userId,
        @Valid @RequestBody UserRoleUpdateRequest request,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.ok(
            "Roles utilisateur mis a jour",
            userService.updateUserRoles(userId, request, principal.getId(), httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        );
    }
}
