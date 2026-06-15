package com.parcinformatique.app.dto.materialrequest;

import com.parcinformatique.app.enums.MaterialRequestStatus;
import com.parcinformatique.app.enums.PriorityLevel;
import java.time.LocalDateTime;
import java.util.UUID;

public record MaterialRequestDto(
    UUID id,
    UUID requesterId,
    String requesterName,
    String requesterEmail,
    String materialType,
    String preferredModel,
    String justification,
    PriorityLevel priority,
    MaterialRequestStatus status,
    String reviewedByName,
    LocalDateTime reviewedAt,
    String reviewComment,
    LocalDateTime createdAt
) {
}
