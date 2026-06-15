package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.dto.maintenance.MaintenanceCardDto;
import com.parcinformatique.app.dto.maintenance.MaintenanceListDto;
import com.parcinformatique.app.dto.maintenance.MaintenanceStatusUpdateRequest;
import com.parcinformatique.app.dto.maintenance.MaintenanceUpsertRequest;
import com.parcinformatique.app.constants.RoleCodes;
import com.parcinformatique.app.entity.ActivityLog;
import com.parcinformatique.app.entity.Equipment;
import com.parcinformatique.app.entity.Maintenance;
import com.parcinformatique.app.entity.Ticket;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.AssignmentStatus;
import com.parcinformatique.app.enums.EquipmentStatus;
import com.parcinformatique.app.enums.MaintenanceStatus;
import com.parcinformatique.app.enums.NotificationType;
import com.parcinformatique.app.enums.TicketStatus;
import com.parcinformatique.app.exception.BusinessException;
import com.parcinformatique.app.exception.ResourceNotFoundException;
import com.parcinformatique.app.mapper.DomainMapper;
import com.parcinformatique.app.repository.ActivityLogRepository;
import com.parcinformatique.app.repository.EquipmentRepository;
import com.parcinformatique.app.repository.MaintenanceRepository;
import com.parcinformatique.app.repository.TicketRepository;
import com.parcinformatique.app.repository.UserRepository;
import com.parcinformatique.app.service.AuditService;
import com.parcinformatique.app.service.MaintenanceService;
import com.parcinformatique.app.service.NotificationService;
import com.parcinformatique.app.utils.CodeGeneratorUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final DomainMapper domainMapper;
    private final CodeGeneratorUtil codeGeneratorUtil;

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceCardDto> getBoard() {
        return maintenanceRepository.findTop8ByOrderByOpenedAtDesc().stream().map(domainMapper::toMaintenanceCard).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceListDto> listMaintenances() {
        return maintenanceRepository.findAllByOrderByOpenedAtDesc().stream().map(domainMapper::toMaintenanceList).toList();
    }

    @Override
    @Transactional
    public MaintenanceCardDto createMaintenance(MaintenanceUpsertRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        Equipment equipment = getEquipment(request.equipmentId());
        boolean employeeRequester = hasRole(actor, RoleCodes.EMPLOYE);
        if (employeeRequester) {
            ensureEmployeeCanRequestMaintenance(actor, equipment);
        }

        User technician = request.technicianId() != null ? getUser(request.technicianId()) : null;
        if (technician != null && !hasRole(technician, RoleCodes.TECHNICIEN)) {
            throw new BusinessException("Le technicien selectionne doit posseder le role technicien");
        }

        MaintenanceStatus requestedStatus = employeeRequester ? MaintenanceStatus.OPEN : request.status();
        if (equipment.getStatus() == EquipmentStatus.RETIRED || equipment.getStatus() == EquipmentStatus.LOST) {
            throw new BusinessException("Cet equipement ne peut pas faire l'objet d'une maintenance");
        }

        Ticket ticket = new Ticket();
        ticket.setReference(codeGeneratorUtil.reference("TCK"));
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setStatus(mapTicketStatus(requestedStatus));
        ticket.setReporter(actor);
        ticket.setTechnician(employeeRequester ? null : technician);
        ticket.setEquipment(equipment);
        ticket.setImpactLevel(request.priority().name());
        ticket.setOpenedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        Maintenance maintenance = new Maintenance();
        maintenance.setReference(codeGeneratorUtil.reference("MNT"));
        maintenance.setEquipment(equipment);
        maintenance.setTechnician(employeeRequester ? null : technician);
        maintenance.setTicket(ticket);
        maintenance.setPriority(request.priority());
        maintenance.setStatus(requestedStatus);
        maintenance.setSlaDeadline(request.slaDeadline());
        maintenance.setCost(request.cost());
        maintenance.setPartsReplaced(request.partsReplaced());
        maintenance.setRootCause(request.rootCause());
        maintenance.setNotes(request.notes());
        if (requestedStatus == MaintenanceStatus.IN_PROGRESS) {
            maintenance.setStartedAt(LocalDateTime.now());
        }
        if (requestedStatus == MaintenanceStatus.RESOLVED || requestedStatus == MaintenanceStatus.CLOSED) {
            maintenance.setResolvedAt(LocalDateTime.now());
        }
        Maintenance saved = maintenanceRepository.save(maintenance);

        equipment.setStatus(EquipmentStatus.MAINTENANCE);
        equipmentRepository.save(equipment);

        if (technician != null) {
            notificationService.create(
                technician,
                "Nouvelle intervention",
                "Une maintenance vous a ete assignee pour " + equipment.getName() + ".",
                NotificationType.MAINTENANCE,
                "/maintenances"
            );
        }
        writeActivity(actor, "create-maintenance", "Maintenance", saved.getId().toString(), "Ouverture de maintenance " + saved.getReference(), "bi bi-tools", "warning");
        auditService.log(actor, "CREATE_MAINTENANCE", "Maintenance", saved.getId().toString(), "Maintenance creee: " + saved.getReference(), ipAddress, userAgent);
        return domainMapper.toMaintenanceCard(saved);
    }

    @Override
    @Transactional
    public MaintenanceCardDto updateStatus(UUID maintenanceId, MaintenanceStatusUpdateRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new ResourceNotFoundException("Maintenance introuvable"));
        maintenance.setStatus(request.status());
        if (request.status() == MaintenanceStatus.IN_PROGRESS && maintenance.getStartedAt() == null) {
            maintenance.setStartedAt(LocalDateTime.now());
        }
        if ((request.status() == MaintenanceStatus.RESOLVED || request.status() == MaintenanceStatus.CLOSED) && maintenance.getResolvedAt() == null) {
            maintenance.setResolvedAt(LocalDateTime.now());
        }
        if (maintenance.getTicket() != null) {
            maintenance.getTicket().setStatus(mapTicketStatus(request.status()));
            if (request.status() == MaintenanceStatus.CLOSED) {
                maintenance.getTicket().setClosedAt(LocalDateTime.now());
            }
        }

        Equipment equipment = maintenance.getEquipment();
        if (request.status() == MaintenanceStatus.RESOLVED || request.status() == MaintenanceStatus.CLOSED) {
            equipment.setStatus(equipment.getActiveAssignment() != null ? EquipmentStatus.ASSIGNED : EquipmentStatus.AVAILABLE);
        } else {
            equipment.setStatus(EquipmentStatus.MAINTENANCE);
        }
        equipmentRepository.save(equipment);

        Maintenance saved = maintenanceRepository.save(maintenance);
        if (saved.getTechnician() != null) {
            notificationService.create(
                saved.getTechnician(),
                "Statut maintenance mis a jour",
                "La maintenance " + saved.getReference() + " est maintenant " + saved.getStatus() + ".",
                NotificationType.INFO,
                "/maintenances"
            );
        }
        writeActivity(actor, "update-maintenance", "Maintenance", saved.getId().toString(), "Statut passe a " + saved.getStatus(), "bi bi-kanban", "info");
        auditService.log(actor, "UPDATE_MAINTENANCE_STATUS", "Maintenance", saved.getId().toString(), "Statut maintenance: " + saved.getStatus(), ipAddress, userAgent);
        return domainMapper.toMaintenanceCard(saved);
    }

    private TicketStatus mapTicketStatus(MaintenanceStatus status) {
        return switch (status) {
            case OPEN -> TicketStatus.OPEN;
            case IN_PROGRESS -> TicketStatus.IN_PROGRESS;
            case ON_HOLD -> TicketStatus.WAITING_PARTS;
            case RESOLVED -> TicketStatus.RESOLVED;
            case CLOSED -> TicketStatus.CLOSED;
        };
    }

    private Equipment getEquipment(UUID equipmentId) {
        return equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Equipement introuvable"));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(role -> roleName.equals(role.getName()));
    }

    private void ensureEmployeeCanRequestMaintenance(User actor, Equipment equipment) {
        if (equipment.getActiveAssignment() == null) {
            throw new BusinessException("Vous ne pouvez declarer une panne que sur un equipement qui vous est affecte");
        }
        if (!equipment.getActiveAssignment().getBeneficiary().getId().equals(actor.getId())) {
            throw new BusinessException("Vous ne pouvez declarer une panne que sur vos propres equipements");
        }
        if (equipment.getActiveAssignment().getStatus() != AssignmentStatus.ACTIVE) {
            throw new BusinessException("L'equipement doit avoir une affectation active avant de pouvoir ouvrir une maintenance");
        }
    }

    private void writeActivity(User actor, String verb, String subjectType, String subjectId, String description, String icon, String color) {
        ActivityLog log = new ActivityLog();
        log.setActor(actor);
        log.setVerb(verb);
        log.setSubjectType(subjectType);
        log.setSubjectId(subjectId);
        log.setDescription(description);
        log.setIcon(icon);
        log.setColor(color);
        activityLogRepository.save(log);
    }
}
