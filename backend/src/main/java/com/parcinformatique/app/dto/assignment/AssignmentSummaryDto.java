package com.parcinformatique.app.dto.assignment;

import com.parcinformatique.app.enums.AssignmentStatus;
import java.time.LocalDate;
import java.util.UUID;

public record AssignmentSummaryDto(
    UUID id,
    UUID equipmentId,
    String equipmentName,
    String inventoryCode,
    String beneficiaryName,
    AssignmentStatus status,
    boolean approved,
    LocalDate startDate,
    LocalDate expectedReturnDate
) {
}
