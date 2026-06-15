package com.parcinformatique.app.service;

import com.parcinformatique.app.dto.maintenance.MaintenanceCardDto;
import com.parcinformatique.app.dto.maintenance.MaintenanceListDto;
import com.parcinformatique.app.dto.maintenance.MaintenanceStatusUpdateRequest;
import com.parcinformatique.app.dto.maintenance.MaintenanceUpsertRequest;
import java.util.List;
import java.util.UUID;

public interface MaintenanceService {

    List<MaintenanceCardDto> getBoard();

    List<MaintenanceListDto> listMaintenances();

    MaintenanceCardDto createMaintenance(MaintenanceUpsertRequest request, UUID actorId, String ipAddress, String userAgent);

    MaintenanceCardDto updateStatus(UUID maintenanceId, MaintenanceStatusUpdateRequest request, UUID actorId, String ipAddress, String userAgent);
}
