package com.benny.board_mate.room;

import com.benny.board_mate.common.config.RedisConfig;
import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.game.BoardGame;
import com.benny.board_mate.game.GameRepository;
import com.benny.board_mate.participant.Participant;
import com.benny.board_mate.participant.ParticipantRepository;
import com.benny.board_mate.room.dto.RoomCreateRequest;
import com.benny.board_mate.room.dto.RoomResponse;
import com.benny.board_mate.room.dto.RoomSearchRequest;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    @CacheEvict(value = RedisConfig.CACHE_ROOMS, allEntries = true)
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

    public Page<RoomResponse> searchRooms(RoomSearchRequest request) {
        // 필터가 있으면 캐시 없이 DB 조회
        if (request.getRegion() != null || request.getGameId() != null || request.getDate() != null) {
            PageRequest pageable = PageRequest.of(
                    request.getPage(),
                    request.getSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
            return roomRepository.findAll(RoomSpecification.searchRooms(request), pageable)
                    .map(RoomResponse::from);
        }

        // 필터 없으면 캐시된 전체 목록에서 페이지네이션
        List<RoomResponse> cachedRooms = getCachedWaitingRooms();
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), cachedRooms.size());

        if (start >= cachedRooms.size()) {
            return Page.empty();
        }

        List<RoomResponse> pageContent = cachedRooms.subList(start, end);
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize());
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, cachedRooms.size());
    }

    @Cacheable(value = RedisConfig.CACHE_ROOMS, key = "'waiting'")
    public List<RoomResponse> getCachedWaitingRooms() {
        return roomRepository.findAll(RoomSpecification.searchRooms(new RoomSearchRequest()),
                Sort.by(Sort.Direction.DESC, "createdAt"))
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
    @CacheEvict(value = RedisConfig.CACHE_ROOMS, allEntries = true)
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

    public List<RoomResponse> getMyRooms(Long userId) {
        return participantRepository.findByUserId(userId).stream()
                .map(Participant::getRoom)
                .filter(room -> room.getDeletedAt() == null)
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(RoomResponse::from)
                .toList();
    }
}