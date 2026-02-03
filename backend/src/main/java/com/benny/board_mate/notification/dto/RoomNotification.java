package com.benny.board_mate.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomNotification {

    private String type;  // JOIN, LEAVE, ROOM_FULL, GAME_START
    private Long roomId;
    private Long userId;
    private String nickname;
    private int currentParticipants;
    private int maxParticipants;
    private String message;
    private LocalDateTime timestamp;

    public static RoomNotification join(Long roomId, Long userId, String nickname, int current, int max) {
        return RoomNotification.builder()
                .type("JOIN")
                .roomId(roomId)
                .userId(userId)
                .nickname(nickname)
                .currentParticipants(current)
                .maxParticipants(max)
                .message(nickname + "님이 참가했습니다.")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RoomNotification leave(Long roomId, Long userId, String nickname, int current, int max) {
        return RoomNotification.builder()
                .type("LEAVE")
                .roomId(roomId)
                .userId(userId)
                .nickname(nickname)
                .currentParticipants(current)
                .maxParticipants(max)
                .message(nickname + "님이 나갔습니다.")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RoomNotification roomFull(Long roomId, int max) {
        return RoomNotification.builder()
                .type("ROOM_FULL")
                .roomId(roomId)
                .currentParticipants(max)
                .maxParticipants(max)
                .message("방이 가득 찼습니다! 게임을 시작할 수 있습니다.")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RoomNotification reminder(Long roomId, String region, String gameTitle, LocalDateTime gameDate) {
        return RoomNotification.builder()
                .type("REMINDER")
                .roomId(roomId)
                .message(String.format("⏰ 1시간 후 '%s'에서 '%s' 게임이 시작됩니다! 잊지 마세요!", region, gameTitle))
                .timestamp(LocalDateTime.now())
                .build();
    }
}