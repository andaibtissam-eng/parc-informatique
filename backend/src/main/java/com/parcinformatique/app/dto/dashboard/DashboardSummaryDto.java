package com.parcinformatique.app.dto.dashboard;

import java.util.List;

public record DashboardSummaryDto(
    long totalEquipments,
    long availableEquipments,
    long assignedEquipments,
    long maintenanceEquipments,
    long activeAssignments,
    long openTickets,
    long unreadNotifications,
    List<String> equipmentChartLabels,
    List<Long> equipmentChartValues,
    List<ActivityItemDto> recentActivity
) {
}
