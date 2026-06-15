package com.parcinformatique.app.service;

import com.parcinformatique.app.dto.materialrequest.MaterialRequestCreateRequest;
import com.parcinformatique.app.dto.materialrequest.MaterialRequestDto;
import com.parcinformatique.app.dto.materialrequest.MaterialRequestReviewRequest;
import java.util.List;
import java.util.UUID;

public interface MaterialRequestService {

    List<MaterialRequestDto> list(UUID actorId);

    MaterialRequestDto create(MaterialRequestCreateRequest request, UUID actorId, String ipAddress, String userAgent);

    MaterialRequestDto review(UUID requestId, MaterialRequestReviewRequest request, UUID actorId, String ipAddress, String userAgent);
}
