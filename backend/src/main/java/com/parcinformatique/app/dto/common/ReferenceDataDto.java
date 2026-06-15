package com.parcinformatique.app.dto.common;

import java.util.List;

public record ReferenceDataDto(
    List<ReferenceOptionDto> departments,
    List<ReferenceOptionDto> categories,
    List<ReferenceOptionDto> locations,
    List<ReferenceOptionDto> suppliers,
    List<ReferenceOptionDto> beneficiaries,
    List<ReferenceOptionDto> technicians,
    List<String> roles
) {
}
