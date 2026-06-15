package com.parcinformatique.app.dto.equipment;

import com.parcinformatique.app.enums.EquipmentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentUpsertRequest(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 120) String brand,
    @Size(max = 120) String model,
    @Size(max = 80) String serialNumber,
    @Size(max = 60) String assetTag,
    @Size(max = 255) String imageUrl,
    @Size(max = 255) String documentUrl,
    @Size(max = 255) String operatingSystem,
    Integer memoryGb,
    Integer storageGb,
    @Size(max = 255) String processor,
    LocalDate purchaseDate,
    LocalDate warrantyEndDate,
    @DecimalMin("0.0") BigDecimal acquisitionCost,
    @NotNull EquipmentStatus status,
    String notes,
    UUID categoryId,
    UUID supplierId,
    UUID locationId,
    UUID departmentId
) {
}
