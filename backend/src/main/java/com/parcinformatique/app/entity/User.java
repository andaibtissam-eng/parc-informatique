package com.parcinformatique.app.entity;

import com.parcinformatique.app.enums.LanguageCode;
import com.parcinformatique.app.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 30)
    private String phone;

    @Column(length = 120)
    private String jobTitle;

    @Column(length = 255)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private LanguageCode language = LanguageCode.FR;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(columnDefinition = "boolean default false")
    private boolean systemAccount = false;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "beneficiary")
    private List<Assignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "assignedBy")
    private List<Assignment> issuedAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "technician")
    private List<Maintenance> maintenancesAsTechnician = new ArrayList<>();

    @OneToMany(mappedBy = "reporter")
    private List<Ticket> ticketsReported = new ArrayList<>();

    @OneToMany(mappedBy = "technician")
    private List<Ticket> ticketsAssigned = new ArrayList<>();

    @OneToMany(mappedBy = "recipient")
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "actor")
    private List<AuditLog> auditLogs = new ArrayList<>();

    @OneToMany(mappedBy = "actor")
    private List<ActivityLog> activityLogs = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<RefreshToken> refreshTokens = new ArrayList<>();
}
