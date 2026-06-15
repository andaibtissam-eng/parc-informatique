package com.parcinformatique.app.dto.user;

import com.parcinformatique.app.enums.LanguageCode;
import com.parcinformatique.app.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record UserUpsertRequest(
    @NotBlank @Size(max = 80) String firstName,
    @NotBlank @Size(max = 80) String lastName,
    @NotBlank @Email @Size(max = 180) String email,
    @Size(max = 30) String phone,
    @Size(max = 120) String jobTitle,
    @Size(max = 255) String avatarUrl,
    UserStatus status,
    LanguageCode language,
    Boolean enabled,
    Boolean emailVerified,
    UUID departmentId,
    @NotEmpty Set<String> roles,
    String password
) {
}
