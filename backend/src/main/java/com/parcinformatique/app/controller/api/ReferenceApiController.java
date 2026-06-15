package com.parcinformatique.app.controller.api;

import com.parcinformatique.app.dto.common.ApiResponse;
import com.parcinformatique.app.dto.common.ReferenceDataDto;
import com.parcinformatique.app.service.ReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/references")
@RequiredArgsConstructor
public class ReferenceApiController {

    private final ReferenceService referenceService;

    @GetMapping
    public ApiResponse<ReferenceDataDto> all() {
        return ApiResponse.ok("Referentiels charges", referenceService.getReferenceData());
    }
}
