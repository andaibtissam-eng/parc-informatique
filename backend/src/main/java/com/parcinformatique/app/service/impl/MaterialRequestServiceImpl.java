package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.constants.RoleCodes;
import com.parcinformatique.app.dto.materialrequest.MaterialRequestCreateRequest;
import com.parcinformatique.app.dto.materialrequest.MaterialRequestDto;
import com.parcinformatique.app.dto.materialrequest.MaterialRequestReviewRequest;
import com.parcinformatique.app.entity.ActivityLog;
import com.parcinformatique.app.entity.MaterialRequest;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.MaterialRequestStatus;
import com.parcinformatique.app.enums.NotificationType;
import com.parcinformatique.app.enums.PriorityLevel;
import com.parcinformatique.app.exception.BusinessException;
import com.parcinformatique.app.exception.ResourceNotFoundException;
import com.parcinformatique.app.repository.ActivityLogRepository;
import com.parcinformatique.app.repository.MaterialRequestRepository;
import com.parcinformatique.app.repository.UserRepository;
import com.parcinformatique.app.service.AuditService;
import com.parcinformatique.app.service.MaterialRequestService;
import com.parcinformatique.app.service.NotificationService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaterialRequestServiceImpl implements MaterialRequestService {

    private final MaterialRequestRepository materialRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaterialRequestDto> list(UUID actorId) {
        User actor = getUser(actorId);
        List<MaterialRequest> requests = canManageRequests(actor)
            ? materialRequestRepository.findAllByOrderByCreatedAtDesc()
            : materialRequestRepository.findByRequesterIdOrderByCreatedAtDesc(actorId);
        return requests.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public MaterialRequestDto create(MaterialRequestCreateRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        if (!hasRole(actor, RoleCodes.EMPLOYE)) {
            throw new BusinessException("Seul un employe beneficiaire peut deposer une demande de materiel");
        }

        MaterialRequest materialRequest = new MaterialRequest();
        materialRequest.setRequester(actor);
        materialRequest.setMaterialType(request.materialType());
        materialRequest.setPreferredModel(request.preferredModel());
        materialRequest.setJustification(request.justification());
        materialRequest.setPriority(request.priority() != null ? request.priority() : PriorityLevel.MEDIUM);
        MaterialRequest saved = materialRequestRepository.save(materialRequest);

        notifyManagers(
            "Nouvelle demande de materiel",
            actor.getFirstName() + " " + actor.getLastName() + " demande: " + saved.getMaterialType(),
            "/assignments"
        );
        writeActivity(actor, "request-material", "MaterialRequest", saved.getId().toString(), "Demande de materiel: " + saved.getMaterialType(), "bi bi-box-seam", "info");
        auditService.log(actor, "CREATE_MATERIAL_REQUEST", "MaterialRequest", saved.getId().toString(), saved.getMaterialType(), ipAddress, userAgent);
        return toDto(saved);
    }

    @Override
    @Transactional
    public MaterialRequestDto review(UUID requestId, MaterialRequestReviewRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUser(actorId);
        if (!canManageRequests(actor)) {
            throw new BusinessException("Vous n'avez pas le droit de traiter cette demande");
        }
        if (request.status() == MaterialRequestStatus.PENDING || request.status() == MaterialRequestStatus.CANCELLED) {
            throw new BusinessException("Choisissez APPROVED, REJECTED ou FULFILLED pour traiter la demande");
        }

        MaterialRequest materialRequest = materialRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Demande de materiel introuvable"));
        materialRequest.setStatus(request.status());
        materialRequest.setReviewedBy(actor);
        materialRequest.setReviewedAt(LocalDateTime.now());
        materialRequest.setReviewComment(request.reviewComment());
        MaterialRequest saved = materialRequestRepository.save(materialRequest);

        notificationService.create(
            saved.getRequester(),
            "Demande de materiel mise a jour",
            "Votre demande " + saved.getMaterialType() + " est maintenant " + saved.getStatus() + ".",
            saved.getStatus() == MaterialRequestStatus.REJECTED ? NotificationType.WARNING : NotificationType.SUCCESS,
            "/assignments"
        );
        writeActivity(actor, "review-material-request", "MaterialRequest", saved.getId().toString(), "Demande " + saved.getStatus() + ": " + saved.getMaterialType(), "bi bi-clipboard-check", "success");
        auditService.log(actor, "REVIEW_MATERIAL_REQUEST", "MaterialRequest", saved.getId().toString(), saved.getStatus().name(), ipAddress, userAgent);
        return toDto(saved);
    }

    private void notifyManagers(String title, String message, String targetUrl) {
        List<User> recipients = new ArrayList<>(userRepository.findByRoles_Name(RoleCodes.ADMINISTRATOR));
        recipients.addAll(userRepository.findByRoles_Name(RoleCodes.RESPONSABLE_INFORMATIQUE));
        recipients.stream().distinct().forEach(user -> notificationService.create(user, title, message, NotificationType.ASSIGNMENT, targetUrl));
    }

    private MaterialRequestDto toDto(MaterialRequest request) {
        User requester = request.getRequester();
        User reviewer = request.getReviewedBy();
        return new MaterialRequestDto(
            request.getId(),
            requester.getId(),
            requester.getFirstName() + " " + requester.getLastName(),
            requester.getEmail(),
            request.getMaterialType(),
            request.getPreferredModel(),
            request.getJustification(),
            request.getPriority(),
            request.getStatus(),
            reviewer != null ? reviewer.getFirstName() + " " + reviewer.getLastName() : null,
            request.getReviewedAt(),
            request.getReviewComment(),
            request.getCreatedAt()
        );
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private boolean canManageRequests(User user) {
        return hasRole(user, RoleCodes.ADMINISTRATOR) || hasRole(user, RoleCodes.RESPONSABLE_INFORMATIQUE);
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(role -> roleName.equals(role.getName()));
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
