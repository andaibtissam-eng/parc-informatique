package com.parcinformatique.app.mapper;

import com.parcinformatique.app.dto.assignment.AssignmentDetailDto;
import com.parcinformatique.app.dto.assignment.AssignmentSummaryDto;
import com.parcinformatique.app.dto.common.NotificationDto;
import com.parcinformatique.app.dto.dashboard.ActivityItemDto;
import com.parcinformatique.app.dto.equipment.EquipmentDetailDto;
import com.parcinformatique.app.dto.equipment.EquipmentSummaryDto;
import com.parcinformatique.app.dto.maintenance.MaintenanceCardDto;
import com.parcinformatique.app.dto.maintenance.MaintenanceListDto;
import com.parcinformatique.app.dto.user.UserDetailDto;
import com.parcinformatique.app.dto.user.UserSummaryDto;
import com.parcinformatique.app.entity.ActivityLog;
import com.parcinformatique.app.entity.Assignment;
import com.parcinformatique.app.entity.Equipment;
import com.parcinformatique.app.entity.Maintenance;
import com.parcinformatique.app.entity.Notification;
import com.parcinformatique.app.entity.User;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DomainMapper {

    public UserSummaryDto toUserSummary(User user) {
        Set<String> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getCode())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        String fullName = user.getFirstName() + " " + user.getLastName();
        String department = user.getDepartment() != null ? user.getDepartment().getName() : null;
        return new UserSummaryDto(
            user.getId(),
            fullName,
            user.getEmail(),
            user.getStatus(),
            user.isEnabled(),
            user.isEmailVerified(),
            department,
            user.getAvatarUrl(),
            roles,
            permissions,
            user.isSystemAccount(),
            user.getCreatedAt(),
            user.getLastLoginAt(),
            user.getFailedLoginAttempts()
        );
    }

    public UserDetailDto toUserDetail(User user) {
        Set<String> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getCode())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        String fullName = user.getFirstName() + " " + user.getLastName();
        return new UserDetailDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            fullName,
            user.getEmail(),
            user.getPhone(),
            user.getJobTitle(),
            user.getAvatarUrl(),
            user.getStatus(),
            user.getLanguage(),
            user.isEnabled(),
            user.isEmailVerified(),
            user.getDepartment() != null ? user.getDepartment().getId() : null,
            user.getDepartment() != null ? user.getDepartment().getName() : null,
            roles,
            permissions,
            user.isSystemAccount(),
            user.getCreatedAt(),
            user.getLastLoginAt(),
            user.getFailedLoginAttempts()
        );
    }

    public EquipmentSummaryDto toEquipmentSummary(Equipment equipment) {
        String category = equipment.getCategory() != null ? equipment.getCategory().getName() : null;
        String location = equipment.getLocation() != null ? equipment.getLocation().getName() : null;
        String currentAssignee = equipment.getActiveAssignment() != null
            ? equipment.getActiveAssignment().getBeneficiary().getFirstName() + " "
                + equipment.getActiveAssignment().getBeneficiary().getLastName()
            : null;
        return new EquipmentSummaryDto(
            equipment.getId(),
            equipment.getInventoryCode(),
            equipment.getName(),
            equipment.getImageUrl(),
            equipment.getBrand(),
            equipment.getModel(),
            equipment.getStatus(),
            category,
            location,
            currentAssignee,
            equipment.getWarrantyEndDate()
        );
    }

    public EquipmentDetailDto toEquipmentDetail(Equipment equipment, String qrCodeImageBase64) {
        return new EquipmentDetailDto(
            equipment.getId(),
            equipment.getInventoryCode(),
            equipment.getName(),
            equipment.getBrand(),
            equipment.getModel(),
            equipment.getSerialNumber(),
            equipment.getAssetTag(),
            equipment.getImageUrl(),
            equipment.getDocumentUrl(),
            equipment.getOperatingSystem(),
            equipment.getMemoryGb(),
            equipment.getStorageGb(),
            equipment.getProcessor(),
            equipment.getPurchaseDate(),
            equipment.getWarrantyEndDate(),
            equipment.getAcquisitionCost(),
            equipment.getStatus(),
            equipment.getNotes(),
            equipment.getCategory() != null ? equipment.getCategory().getId() : null,
            equipment.getCategory() != null ? equipment.getCategory().getName() : null,
            equipment.getSupplier() != null ? equipment.getSupplier().getId() : null,
            equipment.getSupplier() != null ? equipment.getSupplier().getName() : null,
            equipment.getLocation() != null ? equipment.getLocation().getId() : null,
            equipment.getLocation() != null ? equipment.getLocation().getName() : null,
            equipment.getDepartment() != null ? equipment.getDepartment().getId() : null,
            equipment.getDepartment() != null ? equipment.getDepartment().getName() : null,
            equipment.getActiveAssignment() != null
                ? equipment.getActiveAssignment().getBeneficiary().getFirstName() + " "
                    + equipment.getActiveAssignment().getBeneficiary().getLastName()
                : null,
            equipment.getQrCode(),
            qrCodeImageBase64
        );
    }

    public AssignmentSummaryDto toAssignmentSummary(Assignment assignment) {
        return new AssignmentSummaryDto(
            assignment.getId(),
            assignment.getEquipment().getId(),
            assignment.getEquipment().getName(),
            assignment.getEquipment().getInventoryCode(),
            assignment.getBeneficiary().getFirstName() + " " + assignment.getBeneficiary().getLastName(),
            assignment.getStatus(),
            assignment.isApproved(),
            assignment.getStartDate(),
            assignment.getExpectedReturnDate()
        );
    }

    public AssignmentDetailDto toAssignmentDetail(Assignment assignment, boolean returnRecorded) {
        return new AssignmentDetailDto(
            assignment.getId(),
            assignment.getEquipment().getId(),
            assignment.getEquipment().getName(),
            assignment.getEquipment().getInventoryCode(),
            assignment.getBeneficiary().getId(),
            assignment.getBeneficiary().getFirstName() + " " + assignment.getBeneficiary().getLastName(),
            assignment.getAssignedBy().getFirstName() + " " + assignment.getAssignedBy().getLastName(),
            assignment.getValidatedBy() != null
                ? assignment.getValidatedBy().getFirstName() + " " + assignment.getValidatedBy().getLastName()
                : null,
            assignment.getStatus(),
            assignment.isApproved(),
            assignment.getStartDate(),
            assignment.getExpectedReturnDate(),
            assignment.getEndDate(),
            assignment.getDigitalSignature(),
            assignment.getNotes(),
            returnRecorded
        );
    }

    public MaintenanceCardDto toMaintenanceCard(Maintenance maintenance) {
        String technicianName = maintenance.getTechnician() != null
            ? maintenance.getTechnician().getFirstName() + " " + maintenance.getTechnician().getLastName()
            : "Non assigne";
        return new MaintenanceCardDto(
            maintenance.getId(),
            maintenance.getReference(),
            maintenance.getEquipment().getName(),
            technicianName,
            maintenance.getStatus(),
            maintenance.getPriority(),
            maintenance.getSlaDeadline()
        );
    }

    public MaintenanceListDto toMaintenanceList(Maintenance maintenance) {
        String technicianName = maintenance.getTechnician() != null
            ? maintenance.getTechnician().getFirstName() + " " + maintenance.getTechnician().getLastName()
            : "Non assigne";
        return new MaintenanceListDto(
            maintenance.getId(),
            maintenance.getReference(),
            maintenance.getTicket() != null ? maintenance.getTicket().getReference() : null,
            maintenance.getTicket() != null ? maintenance.getTicket().getTitle() : "Intervention technique",
            maintenance.getEquipment().getName(),
            technicianName,
            maintenance.getStatus(),
            maintenance.getPriority(),
            maintenance.getOpenedAt(),
            maintenance.getSlaDeadline()
        );
    }

    public NotificationDto toNotificationDto(Notification notification) {
        return new NotificationDto(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getType(),
            notification.isRead(),
            notification.getTargetUrl(),
            notification.getCreatedAt()
        );
    }

    public ActivityItemDto toActivityItem(ActivityLog log) {
        String actor = log.getActor() != null
            ? log.getActor().getFirstName() + " " + log.getActor().getLastName()
            : "Systeme";
        return new ActivityItemDto(log.getId(), actor, log.getDescription(), log.getIcon(), log.getColor(), log.getOccurredAt());
    }
}
