package com.parcinformatique.app.dto.user;

import com.parcinformatique.app.dto.dashboard.ActivityItemDto;
import java.util.List;

public record UserAdminOverviewDto(
    long totalUsers,
    long pendingUsers,
    long activeUsers,
    long suspendedUsers,
    long inactiveUsers,
    long verifiedUsers,
    long responsableCount,
    long technicianCount,
    long employeeCount,
    List<UserSummaryDto> recentRegistrations,
    List<ActivityItemDto> recentAccountActivity
) {
}
