package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.constants.RoleCodes;
import com.parcinformatique.app.dto.common.PaginatedResponse;
import com.parcinformatique.app.dto.user.UserAdminOverviewDto;
import com.parcinformatique.app.dto.user.UserDetailDto;
import com.parcinformatique.app.dto.user.UserRoleUpdateRequest;
import com.parcinformatique.app.dto.user.UserSummaryDto;
import com.parcinformatique.app.dto.user.UserUpsertRequest;
import com.parcinformatique.app.entity.ActivityLog;
import com.parcinformatique.app.entity.Department;
import com.parcinformatique.app.entity.RefreshToken;
import com.parcinformatique.app.entity.Role;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.LanguageCode;
import com.parcinformatique.app.enums.NotificationType;
import com.parcinformatique.app.enums.UserStatus;
import com.parcinformatique.app.exception.BusinessException;
import com.parcinformatique.app.exception.ResourceNotFoundException;
import com.parcinformatique.app.mapper.DomainMapper;
import com.parcinformatique.app.repository.ActivityLogRepository;
import com.parcinformatique.app.repository.DepartmentRepository;
import com.parcinformatique.app.repository.RefreshTokenRepository;
import com.parcinformatique.app.repository.RoleRepository;
import com.parcinformatique.app.repository.UserRepository;
import com.parcinformatique.app.service.AuditService;
import com.parcinformatique.app.service.NotificationService;
import com.parcinformatique.app.service.UserService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final DomainMapper domainMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<UserSummaryDto> listUsers(String search, UserStatus status, String role, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String normalizedRole = normalizeRoleFilter(role);
        Page<User> result = userRepository.searchUsers(search, status, normalizedRole, pageRequest);
        return new PaginatedResponse<>(
            result.getContent().stream().map(domainMapper::toUserSummary).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminOverviewDto getAdminOverview() {
        return new UserAdminOverviewDto(
            userRepository.count(),
            userRepository.countByStatus(UserStatus.PENDING_APPROVAL) + userRepository.countByStatus(UserStatus.PENDING_VERIFICATION),
            userRepository.countByStatus(UserStatus.ACTIVE),
            userRepository.countByStatus(UserStatus.SUSPENDED) + userRepository.countByStatus(UserStatus.LOCKED),
            userRepository.countByStatus(UserStatus.INACTIVE) + userRepository.countByStatus(UserStatus.REJECTED),
            userRepository.countByEmailVerifiedTrue(),
            userRepository.countByRoles_Name(RoleCodes.RESPONSABLE_INFORMATIQUE),
            userRepository.countByRoles_Name(RoleCodes.TECHNICIEN),
            userRepository.countByRoles_Name(RoleCodes.EMPLOYE),
            userRepository.findTop6ByOrderByCreatedAtDesc().stream().map(domainMapper::toUserSummary).toList(),
            activityLogRepository.findTop8BySubjectTypeInOrderByOccurredAtDesc(List.of("User", "Session"))
                .stream()
                .map(domainMapper::toActivityItem)
                .toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailDto getUser(UUID userId) {
        return domainMapper.toUserDetail(getUserEntity(userId));
    }

    @Override
    @Transactional
    public UserDetailDto createUser(UserUpsertRequest request, UUID actorId, String ipAddress, String userAgent) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Un utilisateur avec cet email existe deja");
        }
        User actor = getUserEntity(actorId);
        User user = new User();
        applyUserData(user, request, true, false);
        User saved = userRepository.save(user);
        writeActivity(actor, "create-user", "User", saved.getId().toString(), "Creation du compte " + saved.getEmail(), "bi bi-person-plus", "success");
        auditService.log(actor, "CREATE_USER", "User", saved.getId().toString(), "Compte cree: " + saved.getEmail(), ipAddress, userAgent);
        return domainMapper.toUserDetail(saved);
    }

    @Override
    @Transactional
    public UserDetailDto updateUser(UUID userId, UserUpsertRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUserEntity(actorId);
        User user = getUserEntity(userId);
        protectSystemAccount(user, "Le compte administrateur principal ne peut pas etre modifie via cette operation");
        Optional<User> existing = userRepository.findByEmailIgnoreCase(request.email());
        if (existing.isPresent() && !existing.get().getId().equals(userId)) {
            throw new BusinessException("Cet email est deja utilise");
        }
        applyUserData(user, request, false, false);
        User saved = userRepository.save(user);
        if (!saved.isEnabled() || saved.getStatus() != UserStatus.ACTIVE) {
            revokeSessions(saved);
        }
        writeActivity(actor, "update-user", "User", saved.getId().toString(), "Mise a jour du compte " + saved.getEmail(), "bi bi-person-gear", "info");
        auditService.log(actor, "UPDATE_USER", "User", saved.getId().toString(), "Compte mis a jour: " + saved.getEmail(), ipAddress, userAgent);
        return domainMapper.toUserDetail(saved);
    }

    @Override
    @Transactional
    public UserSummaryDto approveUser(UUID userId, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUserEntity(actorId);
        User user = getUserEntity(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        User saved = userRepository.save(user);
        notificationService.create(saved, "Compte approuve", "Votre compte a ete valide par l'administrateur.", NotificationType.SUCCESS, "/dashboard");
        writeActivity(actor, "approve-user", "User", saved.getId().toString(), "Compte approuve: " + saved.getEmail(), "bi bi-person-check", "success");
        auditService.log(actor, "APPROVE_USER", "User", saved.getId().toString(), "Compte approuve", ipAddress, userAgent);
        return domainMapper.toUserSummary(saved);
    }

    @Override
    @Transactional
    public UserSummaryDto rejectUser(UUID userId, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUserEntity(actorId);
        User user = getUserEntity(userId);
        protectSystemAccount(user, "Le compte administrateur principal ne peut pas etre rejete");
        user.setStatus(UserStatus.REJECTED);
        user.setEnabled(false);
        User saved = userRepository.save(user);
        revokeSessions(saved);
        notificationService.create(saved, "Compte rejete", "Votre demande d'acces a ete rejetee.", NotificationType.WARNING, "/login");
        writeActivity(actor, "reject-user", "User", saved.getId().toString(), "Compte rejete: " + saved.getEmail(), "bi bi-person-x", "danger");
        auditService.log(actor, "REJECT_USER", "User", saved.getId().toString(), "Compte rejete", ipAddress, userAgent);
        return domainMapper.toUserSummary(saved);
    }

    @Override
    @Transactional
    public UserSummaryDto suspendUser(UUID userId, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUserEntity(actorId);
        User user = getUserEntity(userId);
        protectSystemAccount(user, "Le compte administrateur principal ne peut pas etre suspendu");
        user.setStatus(UserStatus.SUSPENDED);
        user.setEnabled(false);
        User saved = userRepository.save(user);
        revokeSessions(saved);
        notificationService.create(saved, "Compte suspendu", "Votre compte a ete suspendu temporairement.", NotificationType.SECURITY, "/login");
        writeActivity(actor, "suspend-user", "User", saved.getId().toString(), "Compte suspendu: " + saved.getEmail(), "bi bi-person-lock", "warning");
        auditService.log(actor, "SUSPEND_USER", "User", saved.getId().toString(), "Compte suspendu", ipAddress, userAgent);
        return domainMapper.toUserSummary(saved);
    }

    @Override
    @Transactional
    public UserSummaryDto activateUser(UUID userId, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUserEntity(actorId);
        User user = getUserEntity(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        User saved = userRepository.save(user);
        notificationService.create(saved, "Compte active", "Votre compte a ete reactive.", NotificationType.SUCCESS, "/dashboard");
        writeActivity(actor, "activate-user", "User", saved.getId().toString(), "Compte active: " + saved.getEmail(), "bi bi-person-up", "success");
        auditService.log(actor, "ACTIVATE_USER", "User", saved.getId().toString(), "Compte active", ipAddress, userAgent);
        return domainMapper.toUserSummary(saved);
    }

    @Override
    @Transactional
    public UserSummaryDto deactivateUser(UUID userId, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUserEntity(actorId);
        User user = getUserEntity(userId);
        protectSystemAccount(user, "Le compte administrateur principal ne peut pas etre desactive");
        user.setStatus(UserStatus.INACTIVE);
        user.setEnabled(false);
        User saved = userRepository.save(user);
        revokeSessions(saved);
        notificationService.create(saved, "Compte desactive", "Votre compte a ete desactive.", NotificationType.WARNING, "/login");
        writeActivity(actor, "deactivate-user", "User", saved.getId().toString(), "Compte desactive: " + saved.getEmail(), "bi bi-person-dash", "warning");
        auditService.log(actor, "DEACTIVATE_USER", "User", saved.getId().toString(), "Compte desactive", ipAddress, userAgent);
        return domainMapper.toUserSummary(saved);
    }

    @Override
    @Transactional
    public UserSummaryDto updateUserRoles(UUID userId, UserRoleUpdateRequest request, UUID actorId, String ipAddress, String userAgent) {
        User actor = getUserEntity(actorId);
        User user = getUserEntity(userId);
        protectSystemAccount(user, "Le role du compte administrateur principal ne peut pas etre modifie");
        user.setRoles(resolveRoles(request.roles(), false));
        User saved = userRepository.save(user);
        revokeSessions(saved);
        writeActivity(actor, "change-role", "User", saved.getId().toString(), "Roles mis a jour pour " + saved.getEmail(), "bi bi-person-badge", "info");
        auditService.log(actor, "UPDATE_USER_ROLE", "User", saved.getId().toString(), "Roles mis a jour", ipAddress, userAgent);
        return domainMapper.toUserSummary(saved);
    }

    private void applyUserData(User user, UserUpsertRequest request, boolean creating, boolean allowAdministratorRole) {
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email().toLowerCase());
        user.setPhone(request.phone());
        user.setJobTitle(request.jobTitle());
        user.setAvatarUrl(request.avatarUrl());
        user.setStatus(request.status() != null ? request.status() : UserStatus.ACTIVE);
        user.setLanguage(request.language() != null ? request.language() : LanguageCode.FR);
        user.setEnabled(request.enabled() == null || request.enabled());
        user.setEmailVerified(request.emailVerified() != null && request.emailVerified());
        user.setDepartment(resolveDepartment(request.departmentId()));
        user.setRoles(resolveRoles(request.roles(), allowAdministratorRole));

        if (creating) {
            String rawPassword = request.password() != null && !request.password().isBlank() ? request.password() : "Temp@12345";
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        } else if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
    }

    private Set<Role> resolveRoles(Set<String> roleNames, boolean allowAdministratorRole) {
        Set<String> normalized = roleNames.stream()
            .map(String::trim)
            .map(RoleCodes::normalizeAlias)
            .collect(Collectors.toSet());
        if (!allowAdministratorRole && normalized.contains(RoleCodes.ADMINISTRATOR)) {
            throw new BusinessException("Le role administrateur ne peut pas etre assigne via cette operation");
        }
        Set<Role> roles = new HashSet<>(roleRepository.findByNameIn(normalized));
        if (roles.size() != normalized.size()) {
            throw new ResourceNotFoundException("Un ou plusieurs roles sont introuvables");
        }
        return roles;
    }

    private Department resolveDepartment(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Departement introuvable"));
    }

    private String normalizeRoleFilter(String role) {
        if (role == null || role.isBlank() || "ALL".equalsIgnoreCase(role)) {
            return null;
        }
        return RoleCodes.normalizeAlias(role);
    }

    private void revokeSessions(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(user.getId());
        LocalDateTime now = LocalDateTime.now();
        for (RefreshToken token : tokens) {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(now);
            }
        }
        refreshTokenRepository.saveAll(tokens);
    }

    private void protectSystemAccount(User user, String message) {
        if (user.isSystemAccount()) {
            throw new BusinessException(message);
        }
    }

    private User getUserEntity(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
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
