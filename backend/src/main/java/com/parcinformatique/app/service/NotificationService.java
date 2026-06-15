package com.parcinformatique.app.service;

import com.parcinformatique.app.dto.common.NotificationDto;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.NotificationType;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationDto> getLatest(UUID userId);

    NotificationDto create(User recipient, String title, String message, NotificationType type, String targetUrl);

    void markAllAsRead(UUID userId);
}
