package com.benny.board_mate.participant;

import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.notification.NotificationService;
import com.benny.board_mate.notification.dto.RoomNotification;
import com.benny.board_mate.participant.dto.AttendanceCheckRequest;
import com.benny.board_mate.participant.dto.ParticipantResponse;
import com.benny.board_mate.room.Room;
import com.benny.board_mate.room.RoomRepository;
import com.benny.board_mate.trust.TrustScoreService;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TrustScoreService trustScoreService;

    @Transactional
    public ParticipantResponse joinRoom(Long userId, Long roomId) {
        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (participantRepository.existsByRoomAndUser(room, user)) {
            throw new BusinessException(ErrorCode.ROOM_ALREADY_JOINED);
        }

        room.addParticipant();

        Participant participant = Participant.builder()
                .room(room)
                .user(user)
                .build();

        participantRepository.save(participant);

        // 실시간 알림 전송
        notificationService.notifyRoom(roomId, RoomNotification.join(
                roomId,
                userId,
                user.getNickname(),
                room.getCurrentParticipants(),
                room.getMaxParticipants()
        ));

        // 방이 가득 찼으면 추가 알림
        if (room.isFull()) {
            notificationService.notifyRoom(roomId, RoomNotification.roomFull(
                    roomId,
                    room.getMaxParticipants()
            ));
        }

        return ParticipantResponse.from(participant);
    }

    @Transactional
    public void leaveRoom(Long userId, Long roomId) {
        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (room.isHost(user)) {
            throw new BusinessException(ErrorCode.ROOM_HOST_CANNOT_LEAVE);
        }

        Participant participant = participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

        room.removeParticipant();
        participantRepository.delete(participant);

        // 실시간 알림 전송
        notificationService.notifyRoom(roomId, RoomNotification.leave(
                roomId,
                userId,
                user.getNickname(),
                room.getCurrentParticipants(),
                room.getMaxParticipants()
        ));
    }

    public List<ParticipantResponse> getParticipants(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        return participantRepository.findByRoom(room).stream()
                .map(ParticipantResponse::from)
                .toList();
    }

    @Transactional
        public void checkAttendance(Long hostId, Long roomId, AttendanceCheckRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 방장만 출석 체크 가능
        if (!room.isHost(host)) {
                throw new BusinessException(ErrorCode.ROOM_NOT_HOST);
        }

        // 게임 완료 처리
        room.closeRoom();

        // 각 참가자 출석 상태 업데이트 & 신뢰도 반영
        for (AttendanceCheckRequest.AttendanceItem item : request.getAttendances()) {
                Participant participant = participantRepository.findByRoomIdAndUserId(roomId, item.getUserId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

                participant.updateAttendanceStatus(item.getStatus());

                if (item.getStatus() == AttendanceStatus.ATTENDED) {
                trustScoreService.addAttendance(item.getUserId());
                } else if (item.getStatus() == AttendanceStatus.NO_SHOW) {
                trustScoreService.addNoShow(item.getUserId());
                }
        }

        // 출석 체크 완료 알림
        notificationService.notifyRoom(roomId, RoomNotification.builder()
                .type("GAME_CLOSED")
                .roomId(roomId)
                .message("게임이 종료되었습니다. 출석이 반영되었습니다.")
                .timestamp(LocalDateTime.now())
                .build());
        }
}