package com.parcinformatique.app.dto.maintenance;

import com.parcinformatique.app.enums.MaintenanceStatus;
import com.parcinformatique.app.enums.PriorityLevel;
import java.time.LocalDateTime;
import java.util.UUID;

public record MaintenanceListDto(
    UUID id,
    String reference,
    String ticketReference,
    String title,
    String equipmentName,
    String technicianName,
    MaintenanceStatus status,
    PriorityLevel priority,
    LocalDateTime openedAt,
    LocalDateTime slaDeadline
) {
}
