package com.benny.board_mate.notification;

import com.benny.board_mate.notification.dto.RoomNotification;
import com.benny.board_mate.participant.Participant;
import com.benny.board_mate.participant.ParticipantRepository;
import com.benny.board_mate.room.Room;
import com.benny.board_mate.room.RoomRepository;
import com.benny.board_mate.room.RoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final NotificationService notificationService;

    /**
     * 1분마다 실행하여 1시간 후 시작하는 게임의 리마인더를 전송
     */
    @Scheduled(fixedRate = 60000) // 1분마다
    @Transactional
    public void sendGameReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);
        LocalDateTime oneHourOneMinuteLater = oneHourLater.plusMinutes(1);

        // 1시간 후 ~ 1시간 1분 후 사이에 시작하는 방 조회
        List<Room> roomsNeedingReminder = roomRepository.findRoomsNeedingReminder(
                List.of(RoomStatus.WAITING, RoomStatus.FULL),
                oneHourLater,
                oneHourOneMinuteLater
        );

        for (Room room : roomsNeedingReminder) {
            sendReminderToParticipants(room);
            room.markReminderSent();
            log.info("리마인더 전송 완료 - 방 ID: {}, 지역: {}, 게임 시간: {}",
                    room.getId(), room.getRegion(), room.getGameDate());
        }

        if (!roomsNeedingReminder.isEmpty()) {
            log.info("총 {}개 방에 리마인더 전송 완료", roomsNeedingReminder.size());
        }
    }

    private void sendReminderToParticipants(Room room) {
        RoomNotification notification = RoomNotification.reminder(
                room.getId(),
                room.getRegion(),
                room.getGame().getTitle(),
                room.getGameDate()
        );

        // 방 전체에 알림 전송 (WebSocket 구독자들에게)
        notificationService.notifyRoom(room.getId(), notification);

        // 개별 사용자에게도 알림 전송 (개인 알림함용)
        List<Participant> participants = participantRepository.findByRoom(room);
        for (Participant participant : participants) {
            notificationService.notifyUser(participant.getUser().getId(), notification);
        }
    }
}
