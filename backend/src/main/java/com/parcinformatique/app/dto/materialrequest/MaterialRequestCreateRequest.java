package com.parcinformatique.app.dto.materialrequest;

import com.parcinformatique.app.enums.PriorityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaterialRequestCreateRequest(
    @NotBlank @Size(max = 120) String materialType,
    @Size(max = 120) String preferredModel,
    @NotBlank @Size(max = 1500) String justification,
    PriorityLevel priority
) {
}
