package com.parcinformatique.app.dto.equipment;

import com.parcinformatique.app.enums.EquipmentStatus;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentSummaryDto(
    UUID id,
    String inventoryCode,
    String name,
    String imageUrl,
    String brand,
    String model,
    EquipmentStatus status,
    String category,
    String location,
    String currentAssignee,
    LocalDate warrantyEndDate
) {
}
