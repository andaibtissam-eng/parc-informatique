package com.parcinformatique.app.dto.assignment;

import com.parcinformatique.app.enums.AssignmentStatus;
import java.time.LocalDate;
import java.util.UUID;

public record AssignmentDetailDto(
    UUID id,
    UUID equipmentId,
    String equipmentName,
    String inventoryCode,
    UUID beneficiaryId,
    String beneficiaryName,
    String assignedByName,
    String validatedByName,
    AssignmentStatus status,
    boolean approved,
    LocalDate startDate,
    LocalDate expectedReturnDate,
    LocalDate endDate,
    String digitalSignature,
    String notes,
    boolean returnRecorded
) {
}
