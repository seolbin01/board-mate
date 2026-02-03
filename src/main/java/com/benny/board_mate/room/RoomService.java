package com.benny.board_mate.room;

import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.game.BoardGame;
import com.benny.board_mate.game.GameRepository;
import com.benny.board_mate.participant.Participant;
import com.benny.board_mate.participant.ParticipantRepository;
import com.benny.board_mate.room.dto.RoomCreateRequest;
import com.benny.board_mate.room.dto.RoomResponse;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public RoomResponse createRoom(Long userId, RoomCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        BoardGame game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_FOUND));

        Room room = Room.builder()
                .host(user)
                .game(game)
                .region(request.getRegion())
                .cafeName(request.getCafeName())
                .gameDate(request.getGameDate())
                .maxParticipants(request.getMaxParticipants())
                .description(request.getDescription())
                .build();

        roomRepository.save(room);

        // 방장도 참가자로 등록
        participantRepository.save(Participant.builder()
                .room(room)
                .user(user)
                .build());

        return RoomResponse.from(room);
    }

    public List<RoomResponse> getWaitingRooms() {
        return roomRepository.findByRoomStatusOrderByCreatedAtDesc(RoomStatus.WAITING)
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    public RoomResponse getRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return RoomResponse.from(room);
    }

    @Transactional
    public void deleteRoom(Long userId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!room.isHost(user)) {
            throw new BusinessException(ErrorCode.ROOM_NOT_HOST);
        }

        room.softDelete();
    }
}