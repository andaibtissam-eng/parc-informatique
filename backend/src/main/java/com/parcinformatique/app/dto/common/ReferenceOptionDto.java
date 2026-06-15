package com.parcinformatique.app.dto.common;

import java.util.UUID;

public record ReferenceOptionDto(
    UUID id,
    String label,
    String secondary
) {
}
