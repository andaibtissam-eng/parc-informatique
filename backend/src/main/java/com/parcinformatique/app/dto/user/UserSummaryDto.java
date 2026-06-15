package com.parcinformatique.app.dto.user;

import com.parcinformatique.app.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String fullName,
    String email,
    UserStatus status,
    boolean enabled,
    boolean emailVerified,
    String department,
    String avatarUrl,
    Set<String> roles,
    Set<String> permissions,
    boolean systemAccount,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt,
    int failedLoginAttempts
) {
}
