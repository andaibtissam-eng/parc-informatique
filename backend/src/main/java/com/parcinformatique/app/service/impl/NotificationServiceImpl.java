package com.parcinformatique.app.service.impl;

import com.parcinformatique.app.dto.common.NotificationDto;
import com.parcinformatique.app.entity.Notification;
import com.parcinformatique.app.entity.User;
import com.parcinformatique.app.enums.NotificationType;
import com.parcinformatique.app.mapper.DomainMapper;
import com.parcinformatique.app.repository.NotificationRepository;
import com.parcinformatique.app.service.NotificationService;
import com.parcinformatique.app.websocket.NotificationGateway;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final DomainMapper domainMapper;
    private final NotificationGateway notificationGateway;

    @Override
    @Cacheable(cacheNames = "notifications", key = "#userId")
    @Transactional(readOnly = true)
    public List<NotificationDto> getLatest(UUID userId) {
        return notificationRepository.findTop10ByRecipientIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(domainMapper::toNotificationDto)
            .toList();
    }

    @Override
    @CacheEvict(cacheNames = "notifications", key = "#recipient.id")
    @Transactional
    public NotificationDto create(User recipient, String title, String message, NotificationType type, String targetUrl) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetUrl(targetUrl);
        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = domainMapper.toNotificationDto(saved);
        notificationGateway.sendNotification(recipient.getEmail(), dto);
        return dto;
    }

    @Override
    @CacheEvict(cacheNames = "notifications", key = "#userId")
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.findByRecipientIdAndReadFalse(userId).forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
    }
}
