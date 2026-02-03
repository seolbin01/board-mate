package com.benny.board_mate.notification;

import com.benny.board_mate.notification.dto.RoomNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 특정 방의 모든 참가자에게 알림 전송
     * 구독 경로: /topic/rooms/{roomId}
     */
    public void notifyRoom(Long roomId, RoomNotification notification) {
        String destination = "/topic/rooms/" + roomId;
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Room {} 알림 전송: {}", roomId, notification.getMessage());
    }

    /**
     * 특정 유저에게 개인 알림 전송
     * 구독 경로: /queue/notifications
     */
    public void notifyUser(Long userId, RoomNotification notification) {
        String destination = "/queue/notifications";
        messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                destination, 
                notification
        );
        log.info("User {} 알림 전송: {}", userId, notification.getMessage());
    }
}