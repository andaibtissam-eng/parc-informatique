package com.parcinformatique.app.dto.maintenance;

import com.parcinformatique.app.enums.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;

public record MaintenanceStatusUpdateRequest(
    @NotNull MaintenanceStatus status
) {
}
