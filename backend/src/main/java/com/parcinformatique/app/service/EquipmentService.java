package com.parcinformatique.app.service;

import com.parcinformatique.app.dto.common.PaginatedResponse;
import com.parcinformatique.app.dto.equipment.EquipmentDetailDto;
import com.parcinformatique.app.dto.equipment.EquipmentSummaryDto;
import com.parcinformatique.app.dto.equipment.EquipmentUpsertRequest;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface EquipmentService {

    PaginatedResponse<EquipmentSummaryDto> listEquipments(String search, int page, int size);

    EquipmentDetailDto getEquipment(UUID equipmentId);

    EquipmentDetailDto createEquipment(EquipmentUpsertRequest request, UUID actorId, String ipAddress, String userAgent);

    EquipmentDetailDto updateEquipment(UUID equipmentId, EquipmentUpsertRequest request, UUID actorId, String ipAddress, String userAgent);

    EquipmentDetailDto uploadEquipmentImage(UUID equipmentId, MultipartFile file, UUID actorId, String ipAddress, String userAgent);

    EquipmentDetailDto uploadEquipmentDocument(UUID equipmentId, MultipartFile file, UUID actorId, String ipAddress, String userAgent);
}
