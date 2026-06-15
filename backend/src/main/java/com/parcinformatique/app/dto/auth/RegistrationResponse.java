package com.parcinformatique.app.dto.auth;

import com.parcinformatique.app.enums.UserStatus;

public record RegistrationResponse(
    String email,
    String role,
    UserStatus status,
    boolean approvalRequired,
    boolean emailVerificationRequired
) {
}
