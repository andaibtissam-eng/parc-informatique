package com.parcinformatique.app.dto.assignment;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record AssignmentCreateRequest(
    @NotNull UUID equipmentId,
    @NotNull UUID beneficiaryId,
    @NotNull @FutureOrPresent LocalDate startDate,
    LocalDate expectedReturnDate,
    @Size(max = 255) String digitalSignature,
    String notes
) {
}
