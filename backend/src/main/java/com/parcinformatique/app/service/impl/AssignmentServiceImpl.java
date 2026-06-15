package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.constants.RoleCodes;
import com.parcinformatique.app.dto.assignment.AssignmentCreateRequest;
import com.parcinformatique.app.dto.assignment.AssignmentDetailDto;
import com.parcinformatique.app.dto.assignment.AssignmentReturnRequest;
import com.parcinformatique.app.dto.assignment.AssignmentSummaryDto;
import com.parcinformatique.app.dto.common.PaginatedResponse;
import com.parcinformatique.app.entity.ActivityLog;
import com.parcinformatique.app.entity.Assignment;
import com.parcinformatique.app.entity.Equipment;
import com.parcinformatique.app.entity.EquipmentReturn;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.AssignmentStatus;
import com.parcinformatique.app.enums.EquipmentStatus;
import com.parcinformatique.app.enums.NotificationType;
import com.parcinformatique.app.exception.BusinessException;
import com.parcinformatique.app.exception.ResourceNotFoundException;
import com.parcinformatique.app.mapper.DomainMapper;
import com.parcinformatique.app.repository.ActivityLogRepository;
import com.parcinformatique.app.repository.AssignmentRepository;
import com.parcinformatique.app.repository.EquipmentRepository;
import com.parcinformatique.app.repository.ReturnRepository;
import com.parcinformatique.app.repository.UserRepository;
import com.parcinformatique.app.service.AssignmentService;
import com.parcinformatique.app.service.AuditService;
import com.parcinformatique.app.service.NotificationService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final ReturnRepository returnRepository;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final DomainMapper domainMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<AssignmentSummaryDto> listAssignments(UUID actorId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        User actor = getUser(actorId);
        Page<Assignment> result = hasRole(actor, RoleCodes.EMPLOYE)
            ? assignmentRepository.findByBeneficiaryId(actorId, pageRequest)
            : assignmentRepository.findAll(pageRequest);
        return new PaginatedResponse<>(
            result.getContent().stream().map(domainMapper::toAssignmentSummary).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentDetailDto getAssignment(UUID actorId, UUID assignmentId) {
        User actor = getUser(actorId);
        Assignment assignment = hasRole(actor, RoleCodes.EMPLOYE)
            ? assignmentRepository.findByIdAndBeneficiaryId(assignmentId, actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation introuvable"))
            : getAssignmentEntity(assignmentId);
        return domainMapper.toAssignmentDetail(assignment, returnRepository.findByAssignmentId(assignmentId).isPresent());
    }

    @Override
    @Transactional
    public AssignmentDetailDto createAssignment(AssignmentCreateRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        Equipment equipment = getEquipment(request.equipmentId());
        User beneficiary = getUser(request.beneficiaryId());
        ensureEligibleBeneficiary(beneficiary);

        if (equipment.getStatus() == EquipmentStatus.MAINTENANCE) {
            throw new BusinessException("Cet equipement est actuellement en maintenance");
        }
        if (assignmentRepository.existsByEquipmentIdAndStatusIn(
            equipment.getId(),
            List.of(AssignmentStatus.PENDING, AssignmentStatus.ACTIVE)
        )) {
            throw new BusinessException("Une affectation active ou en attente existe deja pour cet equipement");
        }

        Assignment assignment = new Assignment();
        assignment.setEquipment(equipment);
        assignment.setBeneficiary(beneficiary);
        assignment.setAssignedBy(actor);
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setApproved(false);
        assignment.setStartDate(request.startDate());
        assignment.setExpectedReturnDate(request.expectedReturnDate());
        assignment.setDigitalSignature(request.digitalSignature());
        assignment.setNotes(request.notes());
        Assignment saved = assignmentRepository.save(assignment);

        equipment.setActiveAssignment(saved);
        equipment.setStatus(EquipmentStatus.RESERVED);
        equipmentRepository.save(equipment);

        notificationService.create(
            beneficiary,
            "Nouvelle demande d'affectation",
            "Le materiel " + equipment.getInventoryCode() + " vous est reserve pour validation.",
            NotificationType.ASSIGNMENT,
            "/assignments"
        );
        writeActivity(actor, "assign-request", "Assignment", saved.getId().toString(), "Affectation creee pour " + beneficiary.getEmail(), "bi bi-arrow-left-right", "warning");
        auditService.log(actor, "CREATE_ASSIGNMENT", "Assignment", saved.getId().toString(), "Affectation creee pour " + equipment.getInventoryCode(), ipAddress, userAgent);
        return getAssignment(actorId, saved.getId());
    }

    @Override
    @Transactional
    public AssignmentDetailDto approveAssignment(UUID assignmentId, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        Assignment assignment = getAssignmentEntity(assignmentId);
        if (assignment.isApproved() || assignment.getStatus() == AssignmentStatus.ACTIVE) {
            throw new BusinessException("Cette affectation est deja validee");
        }

        assignment.setApproved(true);
        assignment.setValidatedBy(actor);
        assignment.setStatus(AssignmentStatus.ACTIVE);
        Assignment saved = assignmentRepository.save(assignment);

        Equipment equipment = saved.getEquipment();
        equipment.setStatus(EquipmentStatus.ASSIGNED);
        equipment.setActiveAssignment(saved);
        equipmentRepository.save(equipment);

        notificationService.create(
            saved.getBeneficiary(),
            "Affectation validee",
            "Votre affectation pour " + equipment.getName() + " a ete validee.",
            NotificationType.SUCCESS,
            "/assignments"
        );
        writeActivity(actor, "approve-assignment", "Assignment", saved.getId().toString(), "Affectation validee pour " + saved.getBeneficiary().getEmail(), "bi bi-patch-check", "success");
        auditService.log(actor, "APPROVE_ASSIGNMENT", "Assignment", saved.getId().toString(), "Affectation validee pour " + equipment.getInventoryCode(), ipAddress, userAgent);
        return getAssignment(actorId, saved.getId());
    }

    @Override
    @Transactional
    public AssignmentDetailDto returnAssignment(UUID assignmentId, AssignmentReturnRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        Assignment assignment = getAssignmentEntity(assignmentId);
        if (returnRepository.findByAssignmentId(assignmentId).isPresent()) {
            throw new BusinessException("Le retour de cette affectation est deja enregistre");
        }
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BusinessException("Seule une affectation active peut etre restituee");
        }

        User returnedBy = getUser(request.returnedById());
        EquipmentReturn equipmentReturn = new EquipmentReturn();
        equipmentReturn.setAssignment(assignment);
        equipmentReturn.setReturnedBy(returnedBy);
        equipmentReturn.setReceivedBy(actor);
        equipmentReturn.setReturnedAt(request.returnedAt() != null ? request.returnedAt() : LocalDate.now());
        equipmentReturn.setConditionStatus(request.conditionStatus());
        equipmentReturn.setReport(request.report());
        equipmentReturn.setReassignable(request.reassignable());
        returnRepository.save(equipmentReturn);

        assignment.setEndDate(equipmentReturn.getReturnedAt());
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setApproved(true);
        assignmentRepository.save(assignment);

        Equipment equipment = assignment.getEquipment();
        equipment.setActiveAssignment(null);
        equipment.setStatus(request.reassignable() && request.conditionStatus() != com.parcinformatique.app.enums.ReturnCondition.DAMAGED
            ? EquipmentStatus.AVAILABLE
            : EquipmentStatus.MAINTENANCE);
        equipmentRepository.save(equipment);

        notificationService.create(
            assignment.getBeneficiary(),
            "Restitution enregistree",
            "Le retour du materiel " + equipment.getInventoryCode() + " a ete enregistre.",
            NotificationType.INFO,
            "/assignments"
        );
        writeActivity(actor, "return-assignment", "Assignment", assignment.getId().toString(), "Retour du materiel " + equipment.getInventoryCode(), "bi bi-box-arrow-in-left", "info");
        auditService.log(actor, "RETURN_ASSIGNMENT", "Assignment", assignment.getId().toString(), "Retour du materiel " + equipment.getInventoryCode(), ipAddress, userAgent);
        return getAssignment(actorId, assignment.getId());
    }

    private Assignment getAssignmentEntity(UUID assignmentId) {
        return assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Affectation introuvable"));
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

    private void ensureEligibleBeneficiary(User beneficiary) {
        if (!hasRole(beneficiary, RoleCodes.EMPLOYE)) {
            throw new BusinessException("Le beneficiaire selectionne doit etre un employe");
        }
        if (!beneficiary.isEnabled() || !beneficiary.isEmailVerified() || beneficiary.getStatus() != com.parcinformatique.app.enums.UserStatus.ACTIVE) {
            throw new BusinessException("Le beneficiaire doit avoir un compte actif, verifie et approuve");
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
