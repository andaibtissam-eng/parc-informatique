package com.parcinformatique.app.dto.auth;

import com.parcinformatique.app.dto.user.UserSummaryDto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds,
    UserSummaryDto user
) {
}
