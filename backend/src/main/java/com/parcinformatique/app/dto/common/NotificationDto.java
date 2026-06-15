package com.parcinformatique.app.dto.common;

import com.parcinformatique.app.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    String title,
    String message,
    NotificationType type,
    boolean read,
    String targetUrl,
    LocalDateTime createdAt
) {
}
