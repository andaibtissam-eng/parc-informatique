package com.parcinformatique.app.dto.equipment;

import com.parcinformatique.app.enums.EquipmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentDetailDto(
    UUID id,
    String inventoryCode,
    String name,
    String brand,
    String model,
    String serialNumber,
    String assetTag,
    String imageUrl,
    String documentUrl,
    String operatingSystem,
    Integer memoryGb,
    Integer storageGb,
    String processor,
    LocalDate purchaseDate,
    LocalDate warrantyEndDate,
    BigDecimal acquisitionCost,
    EquipmentStatus status,
    String notes,
    UUID categoryId,
    String categoryName,
    UUID supplierId,
    String supplierName,
    UUID locationId,
    String locationName,
    UUID departmentId,
    String departmentName,
    String currentAssignee,
    String qrCodeValue,
    String qrCodeImageBase64
) {
}
