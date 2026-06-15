package com.parcinformatique.app.dto.maintenance;

import com.parcinformatique.app.enums.MaintenanceStatus;
import com.parcinformatique.app.enums.PriorityLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MaintenanceUpsertRequest(
    @NotNull UUID equipmentId,
    UUID technicianId,
    @NotBlank @Size(max = 180) String title,
    @NotBlank String description,
    @NotNull PriorityLevel priority,
    @NotNull MaintenanceStatus status,
    LocalDateTime slaDeadline,
    @DecimalMin("0.0") BigDecimal cost,
    String partsReplaced,
    String rootCause,
    String notes
) {
}
