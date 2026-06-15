package com.parcinformatique.app.dto.maintenance;

import com.parcinformatique.app.enums.MaintenanceStatus;
import com.parcinformatique.app.enums.PriorityLevel;
import java.time.LocalDateTime;
import java.util.UUID;

public record MaintenanceCardDto(
    UUID id,
    String reference,
    String equipmentName,
    String technicianName,
    MaintenanceStatus status,
    PriorityLevel priority,
    LocalDateTime slaDeadline
) {
}
