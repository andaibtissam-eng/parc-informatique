package com.parcinformatique.app.dto.materialrequest;

import com.parcinformatique.app.enums.MaterialRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaterialRequestReviewRequest(
    @NotNull MaterialRequestStatus status,
    @Size(max = 1500) String reviewComment
) {
}
