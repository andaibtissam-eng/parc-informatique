package com.parcinformatique.app.service;

import com.parcinformatique.app.dto.common.PaginatedResponse;
import com.parcinformatique.app.dto.user.UserAdminOverviewDto;
import com.parcinformatique.app.dto.user.UserDetailDto;
import com.parcinformatique.app.dto.user.UserRoleUpdateRequest;
import com.parcinformatique.app.dto.user.UserSummaryDto;
import com.parcinformatique.app.dto.user.UserUpsertRequest;
import com.parcinformatique.app.enums.UserStatus;
import java.util.UUID;

public interface UserService {

    PaginatedResponse<UserSummaryDto> listUsers(String search, UserStatus status, String role, int page, int size);

    UserAdminOverviewDto getAdminOverview();

    UserDetailDto getUser(UUID userId);

    UserDetailDto createUser(UserUpsertRequest request, UUID actorId, String ipAddress, String userAgent);

    UserDetailDto updateUser(UUID userId, UserUpsertRequest request, UUID actorId, String ipAddress, String userAgent);

    UserSummaryDto approveUser(UUID userId, UUID actorId, String ipAddress, String userAgent);

    UserSummaryDto rejectUser(UUID userId, UUID actorId, String ipAddress, String userAgent);

    UserSummaryDto suspendUser(UUID userId, UUID actorId, String ipAddress, String userAgent);

    UserSummaryDto activateUser(UUID userId, UUID actorId, String ipAddress, String userAgent);

    UserSummaryDto deactivateUser(UUID userId, UUID actorId, String ipAddress, String userAgent);

    UserSummaryDto updateUserRoles(UUID userId, UserRoleUpdateRequest request, UUID actorId, String ipAddress, String userAgent);
}
