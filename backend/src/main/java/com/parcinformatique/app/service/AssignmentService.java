package com.parcinformatique.app.service;

import com.parcinformatique.app.dto.assignment.AssignmentCreateRequest;
import com.parcinformatique.app.dto.assignment.AssignmentDetailDto;
import com.parcinformatique.app.dto.assignment.AssignmentReturnRequest;
import com.parcinformatique.app.dto.assignment.AssignmentSummaryDto;
import com.parcinformatique.app.dto.common.PaginatedResponse;
import java.util.UUID;

public interface AssignmentService {

    PaginatedResponse<AssignmentSummaryDto> listAssignments(UUID actorId, int page, int size);

    AssignmentDetailDto getAssignment(UUID actorId, UUID assignmentId);

    AssignmentDetailDto createAssignment(AssignmentCreateRequest request, UUID actorId, String ipAddress, String userAgent);

    AssignmentDetailDto approveAssignment(UUID assignmentId, UUID actorId, String ipAddress, String userAgent);

    AssignmentDetailDto returnAssignment(UUID assignmentId, AssignmentReturnRequest request, UUID actorId, String ipAddress, String userAgent);
}
