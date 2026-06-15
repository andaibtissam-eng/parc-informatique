package com.parcinformatique.app.dto.assignment;

import com.parcinformatique.app.enums.ReturnCondition;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AssignmentReturnRequest(
    @NotNull UUID returnedById,
    LocalDate returnedAt,
    @NotNull ReturnCondition conditionStatus,
    String report,
    boolean reassignable
) {
}
