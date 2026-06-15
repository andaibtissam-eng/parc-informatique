package com.parcinformatique.app.dto.user;

import com.parcinformatique.app.enums.LanguageCode;
import com.parcinformatique.app.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserDetailDto(
    UUID id,
    String firstName,
    String lastName,
    String fullName,
    String email,
    String phone,
    String jobTitle,
    String avatarUrl,
    UserStatus status,
    LanguageCode language,
    boolean enabled,
    boolean emailVerified,
    UUID departmentId,
    String department,
    Set<String> roles,
    Set<String> permissions,
    boolean systemAccount,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt,
    int failedLoginAttempts
) {
}
