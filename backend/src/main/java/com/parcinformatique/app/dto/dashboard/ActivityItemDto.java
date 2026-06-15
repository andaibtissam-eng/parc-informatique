package com.parcinformatique.app.dto.dashboard;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityItemDto(
    UUID id,
    String actor,
    String description,
    String icon,
    String color,
    LocalDateTime occurredAt
) {
}
