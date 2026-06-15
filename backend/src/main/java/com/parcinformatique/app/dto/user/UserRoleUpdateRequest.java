package com.parcinformatique.app.dto.user;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UserRoleUpdateRequest(
    @NotEmpty Set<String> roles
) {
}
