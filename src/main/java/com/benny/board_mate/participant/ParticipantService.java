package com.benny.board_mate.participant;

import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.participant.dto.ParticipantResponse;
import com.benny.board_mate.room.Room;
import com.benny.board_mate.room.RoomRepository;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    /**
     * 방 참가 - 비관적 락(PESSIMISTIC_WRITE)으로 동시성 제어
     * 
     * 왜 비관적 락인가?
     * - 여러 유저가 동시에 마지막 1자리에 참가 신청할 때
     * - SELECT FOR UPDATE로 해당 row를 잠가서 순차 처리
     * - 정원 초과를 원천 차단
     */
    @Transactional
    public ParticipantResponse joinRoom(Long userId, Long roomId) {
        // SELECT ... FOR UPDATE (비관적 락)
        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 참가했는지 확인
        if (participantRepository.existsByRoomAndUser(room, user)) {
            throw new BusinessException(ErrorCode.ROOM_ALREADY_JOINED);
        }

        // 정원 확인 & 인원 추가 (Room 엔티티 비즈니스 로직)
        room.addParticipant();

        Participant participant = Participant.builder()
                .room(room)
                .user(user)
                .build();

        participantRepository.save(participant);

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
    }

    public List<ParticipantResponse> getParticipants(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        return participantRepository.findByRoom(room).stream()
                .map(ParticipantResponse::from)
                .toList();
    }
}