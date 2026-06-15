package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.dto.dashboard.DashboardSummaryDto;
import com.parcinformatique.app.enums.AssignmentStatus;
import com.parcinformatique.app.enums.EquipmentStatus;
import com.parcinformatique.app.enums.TicketStatus;
import com.parcinformatique.app.mapper.DomainMapper;
import com.parcinformatique.app.repository.ActivityLogRepository;
import com.parcinformatique.app.repository.AssignmentRepository;
import com.parcinformatique.app.repository.EquipmentRepository;
import com.parcinformatique.app.repository.MaintenanceRepository;
import com.parcinformatique.app.repository.NotificationRepository;
import com.parcinformatique.app.repository.TicketRepository;
import com.parcinformatique.app.service.DashboardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EquipmentRepository equipmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final TicketRepository ticketRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;
    private final DomainMapper domainMapper;

    @Override
    @Cacheable(cacheNames = "dashboard", key = "#userId")
    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(UUID userId) {
        long totalEquipments = equipmentRepository.count();
        long availableEquipments = equipmentRepository.countByStatus(EquipmentStatus.AVAILABLE);
        long assignedEquipments = equipmentRepository.countByStatus(EquipmentStatus.ASSIGNED);
        long maintenanceEquipments = equipmentRepository.countByStatus(EquipmentStatus.MAINTENANCE);
        long activeAssignments = assignmentRepository.countByStatus(AssignmentStatus.ACTIVE);
        long openTickets = ticketRepository.countByStatus(TicketStatus.OPEN);
        long unreadNotifications = notificationRepository.countByRecipientIdAndReadFalse(userId);

        List<String> labels = List.of("Disponibles", "Affectés", "Maintenance");
        List<Long> values = List.of(availableEquipments, assignedEquipments, maintenanceEquipments);

        return new DashboardSummaryDto(
            totalEquipments,
            availableEquipments,
            assignedEquipments,
            maintenanceEquipments,
            activeAssignments,
            openTickets,
            unreadNotifications,
            labels,
            values,
            activityLogRepository.findTop12ByOrderByOccurredAtDesc().stream().map(domainMapper::toActivityItem).toList()
        );
    }
}
