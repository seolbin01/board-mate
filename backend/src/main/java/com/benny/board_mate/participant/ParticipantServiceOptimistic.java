package com.benny.board_mate.participant;

import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.participant.dto.ParticipantResponse;
import com.benny.board_mate.room.Room;
import com.benny.board_mate.room.RoomRepository;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipantServiceOptimistic {

    private final ParticipantRepository participantRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private static final int MAX_RETRY = 3;

    /**
     * 방 참가 - 낙관적 락(@Version)으로 동시성 제어
     * 
     * 작동 방식:
     * - Room 엔티티에 @Version 필드 사용
     * - UPDATE 시 version 불일치하면 OptimisticLockException 발생
     * - 재시도 로직으로 충돌 해결
     * 
     * 장점: 락 대기 없이 빠름, 충돌이 적은 경우 효율적
     * 단점: 충돌 많으면 재시도 오버헤드, 정합성 보장 어려움
     */
    @Transactional
    public ParticipantResponse joinRoomWithRetry(Long userId, Long roomId) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRY) {
            try {
                return joinRoom(userId, roomId);
            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount++;
                if (retryCount >= MAX_RETRY) {
                    throw new BusinessException(ErrorCode.ROOM_FULL);
                }
            }
        }
        
        throw new BusinessException(ErrorCode.ROOM_FULL);
    }

    @Transactional
    public ParticipantResponse joinRoom(Long userId, Long roomId) {
        // 일반 조회 (락 없음)
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (participantRepository.existsByRoomAndUser(room, user)) {
            throw new BusinessException(ErrorCode.ROOM_ALREADY_JOINED);
        }

        // 정원 확인 & 인원 추가
        // @Version에 의해 동시 수정 시 OptimisticLockException 발생
        room.addParticipant();

        Participant participant = Participant.builder()
                .room(room)
                .user(user)
                .build();

        participantRepository.save(participant);

        return ParticipantResponse.from(participant);
    }
}