package com.parcinformatique.app.controller.api;

import com.parcinformatique.app.dto.common.ApiResponse;
import com.parcinformatique.app.dto.common.NotificationDto;
import com.parcinformatique.app.security.UserPrincipal;
import com.parcinformatique.app.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAuthority('notifications.read.own')")
    public ApiResponse<List<NotificationDto>> latest(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok("Notifications recentes", notificationService.getLatest(principal.getId()));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasAuthority('notifications.read.own')")
    public ApiResponse<Void> readAll(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return ApiResponse.ok("Notifications marquees comme lues", null);
    }
}
