package com.parcinformatique.app.websocket;

import com.parcinformatique.app.dto.common.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationGateway {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String username, NotificationDto notification) {
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);
    }
}
