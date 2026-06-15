package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.constants.RoleCodes;
import com.parcinformatique.app.dto.auth.AuthResponse;
import com.parcinformatique.app.dto.auth.ForgotPasswordRequest;
import com.parcinformatique.app.dto.auth.LoginRequest;
import com.parcinformatique.app.dto.auth.RefreshTokenRequest;
import com.parcinformatique.app.dto.auth.RegisterRequest;
import com.parcinformatique.app.dto.auth.RegistrationResponse;
import com.parcinformatique.app.dto.auth.ResetPasswordRequest;
import com.parcinformatique.app.dto.auth.VerifyEmailRequest;
import com.parcinformatique.app.entity.ActivityLog;
import com.parcinformatique.app.entity.Department;
import com.parcinformatique.app.entity.EmailVerificationToken;
import com.parcinformatique.app.entity.PasswordResetToken;
import com.parcinformatique.app.entity.RefreshToken;
import com.parcinformatique.app.entity.Role;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.NotificationType;
import com.parcinformatique.app.enums.UserStatus;
import com.parcinformatique.app.exception.BusinessException;
import com.parcinformatique.app.exception.ResourceNotFoundException;
import com.parcinformatique.app.exception.UnauthorizedException;
import com.parcinformatique.app.mapper.DomainMapper;
import com.parcinformatique.app.repository.ActivityLogRepository;
import com.parcinformatique.app.repository.DepartmentRepository;
import com.parcinformatique.app.repository.EmailVerificationTokenRepository;
import com.parcinformatique.app.repository.PasswordResetTokenRepository;
import com.parcinformatique.app.repository.RefreshTokenRepository;
import com.parcinformatique.app.repository.RoleRepository;
import com.parcinformatique.app.repository.UserRepository;
import com.parcinformatique.app.config.JwtProperties;
import com.parcinformatique.app.security.JwtTokenProvider;
import com.parcinformatique.app.security.UserPrincipal;
import com.parcinformatique.app.service.AuditService;
import com.parcinformatique.app.service.AuthService;
import com.parcinformatique.app.service.MailService;
import com.parcinformatique.app.service.NotificationService;
import com.parcinformatique.app.utils.CodeGeneratorUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final DomainMapper domainMapper;
    private final CodeGeneratorUtil codeGeneratorUtil;
    private final MailService mailService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            UserPrincipal principal = (UserPrincipal) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            ).getPrincipal();

            User user = principal.getUser();
            ensureUserCanAccess(user);
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            writeActivity(user, "login", "Session", user.getId().toString(), "Connexion reussie pour " + user.getEmail(), "bi bi-box-arrow-in-right", "success");
            auditService.log(user, "LOGIN_SUCCESS", "User", user.getId().toString(), "Connexion reussie", httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));

            return buildAuthResponse(user, httpRequest);
        } catch (BadCredentialsException ex) {
            handleFailedLogin(request.email(), httpRequest, "Identifiants invalides");
            throw new UnauthorizedException("Email ou mot de passe invalide");
        } catch (DisabledException | LockedException ex) {
            User user = userRepository.findByEmailIgnoreCase(request.email().toLowerCase()).orElse(null);
            String message = buildBlockedAccessMessage(user);
            handleFailedLogin(request.email(), httpRequest, message);
            throw new UnauthorizedException(message);
        }
    }

    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Un utilisateur avec cet email existe deja");
        }

        Role requestedRole = resolveRegistrationRole(request.role());

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(request.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEnabled(true);
        user.setSystemAccount(false);

        if (request.departmentCode() != null && !request.departmentCode().isBlank()) {
            Department department = departmentRepository.findByCode(request.departmentCode().trim())
                .orElse(null);
            user.setDepartment(department);
        }

        user.setRoles(new HashSet<>(Set.of(requestedRole)));
        User savedUser = userRepository.save(user);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(savedUser);
        token.setToken(codeGeneratorUtil.reference("VERIFY"));
        token.setExpiresAt(LocalDateTime.now().plusDays(2));
        emailVerificationTokenRepository.save(token);
        mailService.sendVerificationEmail(savedUser, token);

        notificationService.create(
            userRepository.findBySystemAccountTrue().orElse(savedUser),
            "Nouvelle demande d'inscription",
            savedUser.getFirstName() + " " + savedUser.getLastName() + " a demande le role " + requestedRole.getName() + ".",
            NotificationType.INFO,
            "/users"
        );
        writeActivity(savedUser, "register", "User", savedUser.getId().toString(), "Nouvelle inscription en attente pour " + savedUser.getEmail(), "bi bi-person-plus", "warning");
        auditService.log(savedUser, "REGISTER_USER", "User", savedUser.getId().toString(), "Inscription en attente d'approbation", httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));

        return new RegistrationResponse(
            savedUser.getEmail(),
            requestedRole.getName(),
            savedUser.getStatus(),
            true,
            true
        );
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new ResourceNotFoundException("Refresh token introuvable"));
        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token expire ou revoque");
        }
        ensureUserCanAccess(refreshToken.getUser());
        return buildAuthResponse(refreshToken.getUser(), httpRequest);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        ensureUserCanAccess(user);
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(codeGeneratorUtil.reference("RESET"));
        token.setExpiresAt(LocalDateTime.now().plusHours(3));
        passwordResetTokenRepository.save(token);
        mailService.sendPasswordResetEmail(user, token);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.token())
            .orElseThrow(() -> new ResourceNotFoundException("Token de reinitialisation introuvable"));
        if (token.getExpiresAt().isBefore(LocalDateTime.now()) || token.getUsedAt() != null) {
            throw new BusinessException("Le token de reinitialisation n'est plus valide");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(request.token())
            .orElseThrow(() -> new ResourceNotFoundException("Token de verification introuvable"));
        if (token.getExpiresAt().isBefore(LocalDateTime.now()) || token.getVerifiedAt() != null) {
            throw new BusinessException("Le token de verification n'est plus valide");
        }
        User user = token.getUser();
        user.setEmailVerified(true);
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            user.setStatus(UserStatus.PENDING_APPROVAL);
        }
        userRepository.save(user);
        token.setVerifiedAt(LocalDateTime.now());
        emailVerificationTokenRepository.save(token);
    }

    private Role resolveRegistrationRole(String roleName) {
        String normalized = RoleCodes.normalizeAlias(roleName);
        List<String> allowedRoles = List.of(RoleCodes.RESPONSABLE_INFORMATIQUE, RoleCodes.TECHNICIEN, RoleCodes.EMPLOYE);
        if (!allowedRoles.contains(normalized)) {
            throw new BusinessException("Le role demande n'est pas autorise lors de l'inscription");
        }
        return roleRepository.findByName(normalized)
            .orElseThrow(() -> new ResourceNotFoundException("Role introuvable: " + normalized));
    }

    private void ensureUserCanAccess(User user) {
        if (user == null) {
            throw new UnauthorizedException("Utilisateur introuvable");
        }
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Votre adresse email doit etre verifiee avant la connexion");
        }
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Votre compte est desactive");
        }
        switch (user.getStatus()) {
            case ACTIVE -> {
                return;
            }
            case PENDING_APPROVAL, PENDING_VERIFICATION -> throw new UnauthorizedException("Votre compte est en attente de validation par l'administrateur");
            case REJECTED -> throw new UnauthorizedException("Votre demande d'acces a ete rejetee");
            case SUSPENDED -> throw new UnauthorizedException("Votre compte a ete suspendu");
            case INACTIVE -> throw new UnauthorizedException("Votre compte est inactif");
            case LOCKED -> throw new UnauthorizedException("Votre compte est verrouille a la suite de tentatives suspectes");
        }
    }

    private String buildBlockedAccessMessage(User user) {
        if (user == null) {
            return "Acces refuse";
        }
        if (!user.isEmailVerified()) {
            return "Votre adresse email doit etre verifiee avant la connexion";
        }
        if (!user.isEnabled()) {
            return "Votre compte est desactive";
        }
        return switch (user.getStatus()) {
            case PENDING_APPROVAL, PENDING_VERIFICATION -> "Votre compte est en attente de validation par l'administrateur";
            case REJECTED -> "Votre demande d'acces a ete rejetee";
            case SUSPENDED -> "Votre compte a ete suspendu";
            case INACTIVE -> "Votre compte est inactif";
            case LOCKED -> "Votre compte est verrouille a la suite de tentatives suspectes";
            case ACTIVE -> "Acces refuse";
        };
    }

    private void handleFailedLogin(String email, HttpServletRequest httpRequest, String details) {
        userRepository.findByEmailIgnoreCase(email.toLowerCase()).ifPresent(user -> {
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);
            if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS && user.getStatus() == UserStatus.ACTIVE) {
                user.setStatus(UserStatus.LOCKED);
                user.setEnabled(false);
            }
            userRepository.save(user);
            writeActivity(user, "failed-login", "Session", user.getId().toString(), "Tentative de connexion echouee pour " + user.getEmail(), "bi bi-shield-lock", "danger");
            auditService.log(user, "LOGIN_FAILED", "User", user.getId().toString(), details, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        });
    }

    private AuthResponse buildAuthResponse(User user, HttpServletRequest httpRequest) {
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(jwtProperties.getRefreshTokenExpirationDays()));
        refreshToken.setDeviceInfo(httpRequest.getHeader("User-Agent"));
        refreshToken.setIpAddress(httpRequest.getRemoteAddr());
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
            accessToken,
            refreshTokenValue,
            "Bearer",
            jwtTokenProvider.getAccessTokenExpiresInSeconds(),
            domainMapper.toUserSummary(user)
        );
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
