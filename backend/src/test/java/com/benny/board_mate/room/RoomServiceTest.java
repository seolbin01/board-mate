package com.benny.board_mate.room;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomService 단위 테스트")
class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private User testUser;
    private BoardGame testGame;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("host@example.com")
                .password("password")
                .nickname("방장")
                .role("USER")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testGame = BoardGame.builder()
                .title("테스트 게임")
                .minPlayers(2)
                .maxPlayers(4)
                .build();
        ReflectionTestUtils.setField(testGame, "id", 1L);

        testRoom = Room.builder()
                .host(testUser)
                .game(testGame)
                .region("서울 강남")
                .cafeName("보드게임카페")
                .gameDate(LocalDateTime.now().plusDays(1))
                .maxParticipants(4)
                .description("재밌게 합시다")
                .build();
        ReflectionTestUtils.setField(testRoom, "id", 1L);
    }

    @Nested
    @DisplayName("방 생성")
    class CreateRoom {

        @Test
        @DisplayName("성공 - 방을 생성하고 방장을 참가자로 등록한다")
        void createRoom_Success() {
            // given
            Long userId = 1L;
            RoomCreateRequest request = new RoomCreateRequest();
            request.setGameId(1L);
            request.setRegion("서울 강남");
            request.setCafeName("보드게임카페");
            request.setGameDate(LocalDateTime.now().plusDays(1));
            request.setMaxParticipants(4);
            request.setDescription("재밌게 합시다");

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(gameRepository.findById(request.getGameId())).willReturn(Optional.of(testGame));

            // when
            RoomResponse response = roomService.createRoom(userId, request);

            // then
            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository).save(roomCaptor.capture());

            Room savedRoom = roomCaptor.getValue();
            assertThat(savedRoom.getRegion()).isEqualTo(request.getRegion());
            assertThat(savedRoom.getCafeName()).isEqualTo(request.getCafeName());
            assertThat(savedRoom.getMaxParticipants()).isEqualTo(request.getMaxParticipants());
            assertThat(savedRoom.getCurrentParticipants()).isEqualTo(1);
            assertThat(savedRoom.getRoomStatus()).isEqualTo(RoomStatus.WAITING);

            ArgumentCaptor<Participant> participantCaptor = ArgumentCaptor.forClass(Participant.class);
            verify(participantRepository).save(participantCaptor.capture());

            Participant savedParticipant = participantCaptor.getValue();
            assertThat(savedParticipant.getUser()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자로 방 생성 시 예외 발생")
        void createRoom_UserNotFound() {
            // given
            Long userId = 999L;
            RoomCreateRequest request = new RoomCreateRequest();
            request.setGameId(1L);

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomService.createRoom(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게임으로 방 생성 시 예외 발생")
        void createRoom_GameNotFound() {
            // given
            Long userId = 1L;
            RoomCreateRequest request = new RoomCreateRequest();
            request.setGameId(999L);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(gameRepository.findById(request.getGameId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomService.createRoom(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GAME_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("방 목록 조회")
    class GetRooms {

        @Test
        @DisplayName("성공 - 대기 중인 방 목록을 조회한다")
        void getWaitingRooms_Success() {
            // given
            List<Room> rooms = List.of(testRoom);
            given(roomRepository.findByRoomStatusOrderByCreatedAtDesc(RoomStatus.WAITING))
                    .willReturn(rooms);

            // when
            List<RoomResponse> result = roomService.getWaitingRooms();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRegion()).isEqualTo(testRoom.getRegion());
        }

        @Test
        @DisplayName("성공 - 검색 조건으로 방 목록을 페이징 조회한다")
        @SuppressWarnings("unchecked")
        void searchRooms_Success() {
            // given
            RoomSearchRequest request = new RoomSearchRequest();
            request.setRegion("강남");
            request.setPage(0);
            request.setSize(10);

            Page<Room> roomPage = new PageImpl<>(List.of(testRoom));
            given(roomRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(roomPage);

            // when
            Page<RoomResponse> result = roomService.searchRooms(request);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("방 상세 조회")
    class GetRoom {

        @Test
        @DisplayName("성공 - 방 ID로 방 정보를 조회한다")
        void getRoom_Success() {
            // given
            Long roomId = 1L;
            given(roomRepository.findById(roomId)).willReturn(Optional.of(testRoom));

            // when
            RoomResponse result = roomService.getRoom(roomId);

            // then
            assertThat(result.getRegion()).isEqualTo(testRoom.getRegion());
            assertThat(result.getCafeName()).isEqualTo(testRoom.getCafeName());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 방 조회 시 예외 발생")
        void getRoom_NotFound() {
            // given
            Long roomId = 999L;
            given(roomRepository.findById(roomId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomService.getRoom(roomId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("방 삭제")
    class DeleteRoom {

        @Test
        @DisplayName("성공 - 방장이 방을 삭제한다")
        void deleteRoom_Success() {
            // given
            Long userId = 1L;
            Long roomId = 1L;

            given(roomRepository.findById(roomId)).willReturn(Optional.of(testRoom));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

            // when
            roomService.deleteRoom(userId, roomId);

            // then
            assertThat(testRoom.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 방장이 아닌 사용자가 방 삭제 시 예외 발생")
        void deleteRoom_NotHost() {
            // given
            Long userId = 2L;
            Long roomId = 1L;

            User otherUser = User.builder()
                    .email("other@example.com")
                    .password("password")
                    .nickname("다른유저")
                    .role("USER")
                    .build();
            ReflectionTestUtils.setField(otherUser, "id", 2L);

            given(roomRepository.findById(roomId)).willReturn(Optional.of(testRoom));
            given(userRepository.findById(userId)).willReturn(Optional.of(otherUser));

            // when & then
            assertThatThrownBy(() -> roomService.deleteRoom(userId, roomId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_NOT_HOST);
        }
    }

    @Nested
    @DisplayName("내가 참가한 방 목록 조회")
    class GetMyRooms {

        @Test
        @DisplayName("성공 - 사용자가 참가한 방 목록을 조회한다")
        void getMyRooms_Success() {
            // given
            Long userId = 1L;
            Participant participant = Participant.builder()
                    .room(testRoom)
                    .user(testUser)
                    .build();

            given(participantRepository.findByUserId(userId)).willReturn(List.of(participant));

            // when
            List<RoomResponse> result = roomService.getMyRooms(userId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRegion()).isEqualTo(testRoom.getRegion());
        }
    }
}
