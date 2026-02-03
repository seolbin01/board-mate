package com.benny.board_mate.participant;

import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.game.BoardGame;
import com.benny.board_mate.game.GameRepository;
import com.benny.board_mate.participant.dto.ParticipantResponse;
import com.benny.board_mate.room.Room;
import com.benny.board_mate.room.RoomRepository;
import com.benny.board_mate.room.RoomStatus;
import com.benny.board_mate.trust.TrustScore;
import com.benny.board_mate.trust.TrustScoreRepository;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("방 참가 통합 테스트")
class ParticipantIntegrationTest {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private TrustScoreRepository trustScoreRepository;

    private User hostUser;
    private User participantUser;
    private BoardGame game;
    private Room room;

    @BeforeEach
    void setUp() {
        // 방장 생성
        hostUser = userRepository.save(User.builder()
                .email("host@example.com")
                .password("password123")
                .nickname("방장")
                .role("USER")
                .build());
        trustScoreRepository.save(new TrustScore(hostUser));

        // 참가자 생성
        participantUser = userRepository.save(User.builder()
                .email("participant@example.com")
                .password("password123")
                .nickname("참가자")
                .role("USER")
                .build());
        trustScoreRepository.save(new TrustScore(participantUser));

        // 게임 생성
        game = gameRepository.save(BoardGame.builder()
                .title("테스트 게임")
                .minPlayers(2)
                .maxPlayers(4)
                .build());

        // 방 생성
        room = roomRepository.save(Room.builder()
                .host(hostUser)
                .game(game)
                .region("서울 강남")
                .cafeName("보드게임카페")
                .gameDate(LocalDateTime.now().plusDays(1))
                .maxParticipants(4)
                .description("재밌게 합시다")
                .build());

        // 방장을 참가자로 등록
        participantRepository.save(Participant.builder()
                .room(room)
                .user(hostUser)
                .build());
    }

    @Nested
    @DisplayName("방 참가")
    class JoinRoom {

        @Test
        @DisplayName("성공 - 새로운 사용자가 방에 참가한다")
        void joinRoom_Success() {
            // when
            ParticipantResponse response = participantService.joinRoom(participantUser.getId(), room.getId());

            // then
            assertThat(response.getNickname()).isEqualTo("참가자");

            Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();
            assertThat(updatedRoom.getCurrentParticipants()).isEqualTo(2);
        }

        @Test
        @DisplayName("실패 - 이미 참가한 사용자가 다시 참가 시도")
        void joinRoom_AlreadyJoined() {
            // given
            participantRepository.save(Participant.builder()
                    .room(room)
                    .user(participantUser)
                    .build());

            // when & then
            assertThatThrownBy(() -> participantService.joinRoom(participantUser.getId(), room.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_ALREADY_JOINED);
        }

        @Test
        @DisplayName("실패 - 방이 가득 찬 경우")
        void joinRoom_RoomFull() {
            // given
            room.addParticipant();
            room.addParticipant();
            room.addParticipant(); // currentParticipants = 4 (max)
            roomRepository.save(room);

            // when & then
            assertThatThrownBy(() -> participantService.joinRoom(participantUser.getId(), room.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_FULL);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 방에 참가 시도")
        void joinRoom_RoomNotFound() {
            // when & then
            assertThatThrownBy(() -> participantService.joinRoom(participantUser.getId(), 99999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자가 참가 시도")
        void joinRoom_UserNotFound() {
            // when & then
            assertThatThrownBy(() -> participantService.joinRoom(99999L, room.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("방 나가기")
    class LeaveRoom {

        @BeforeEach
        void setUp() {
            // 참가자를 방에 등록
            participantRepository.save(Participant.builder()
                    .room(room)
                    .user(participantUser)
                    .build());
            room.addParticipant();
            roomRepository.save(room);
        }

        @Test
        @DisplayName("성공 - 참가자가 방에서 나간다")
        void leaveRoom_Success() {
            // when
            participantService.leaveRoom(participantUser.getId(), room.getId());

            // then
            Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();
            assertThat(updatedRoom.getCurrentParticipants()).isEqualTo(1);
            assertThat(participantRepository.existsByRoomAndUser(room, participantUser)).isFalse();
        }

        @Test
        @DisplayName("실패 - 방장이 나가기 시도")
        void leaveRoom_HostCannotLeave() {
            // when & then
            assertThatThrownBy(() -> participantService.leaveRoom(hostUser.getId(), room.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_HOST_CANNOT_LEAVE);
        }

        @Test
        @DisplayName("실패 - 참가하지 않은 사용자가 나가기 시도")
        void leaveRoom_NotParticipant() {
            // given
            User otherUser = userRepository.save(User.builder()
                    .email("other@example.com")
                    .password("password123")
                    .nickname("다른유저")
                    .role("USER")
                    .build());

            // when & then
            assertThatThrownBy(() -> participantService.leaveRoom(otherUser.getId(), room.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARTICIPANT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("참가자 목록 조회")
    class GetParticipants {

        @Test
        @DisplayName("성공 - 방의 참가자 목록을 조회한다")
        void getParticipants_Success() {
            // given
            participantRepository.save(Participant.builder()
                    .room(room)
                    .user(participantUser)
                    .build());

            // when
            List<ParticipantResponse> result = participantService.getParticipants(room.getId());

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ParticipantResponse::getNickname)
                    .containsExactlyInAnyOrder("방장", "참가자");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 방의 참가자 조회")
        void getParticipants_RoomNotFound() {
            // when & then
            assertThatThrownBy(() -> participantService.getParticipants(99999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("방 상태 변경")
    class RoomStatusChange {

        @Test
        @DisplayName("성공 - 방이 가득 차면 FULL 상태가 된다")
        void roomStatusChange_ToFull() {
            // given
            User user2 = userRepository.save(User.builder()
                    .email("user2@example.com")
                    .password("password123")
                    .nickname("참가자2")
                    .role("USER")
                    .build());
            trustScoreRepository.save(new TrustScore(user2));

            User user3 = userRepository.save(User.builder()
                    .email("user3@example.com")
                    .password("password123")
                    .nickname("참가자3")
                    .role("USER")
                    .build());
            trustScoreRepository.save(new TrustScore(user3));

            // when - 3명 추가 참가 (현재 1 + 3 = 4, max = 4)
            participantService.joinRoom(participantUser.getId(), room.getId());
            participantService.joinRoom(user2.getId(), room.getId());
            participantService.joinRoom(user3.getId(), room.getId());

            // then
            Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();
            assertThat(updatedRoom.getRoomStatus()).isEqualTo(RoomStatus.FULL);
            assertThat(updatedRoom.getCurrentParticipants()).isEqualTo(4);
        }

        @Test
        @DisplayName("성공 - 참가자가 나가면 FULL에서 WAITING으로 변경된다")
        void roomStatusChange_ToWaiting() {
            // given - 방을 가득 채움
            participantService.joinRoom(participantUser.getId(), room.getId());

            User user2 = userRepository.save(User.builder()
                    .email("user2@example.com")
                    .password("password123")
                    .nickname("참가자2")
                    .role("USER")
                    .build());
            trustScoreRepository.save(new TrustScore(user2));

            User user3 = userRepository.save(User.builder()
                    .email("user3@example.com")
                    .password("password123")
                    .nickname("참가자3")
                    .role("USER")
                    .build());
            trustScoreRepository.save(new TrustScore(user3));

            participantService.joinRoom(user2.getId(), room.getId());
            participantService.joinRoom(user3.getId(), room.getId());

            assertThat(roomRepository.findById(room.getId()).orElseThrow().getRoomStatus())
                    .isEqualTo(RoomStatus.FULL);

            // when - 한 명 나감
            participantService.leaveRoom(participantUser.getId(), room.getId());

            // then
            Room updatedRoom = roomRepository.findById(room.getId()).orElseThrow();
            assertThat(updatedRoom.getRoomStatus()).isEqualTo(RoomStatus.WAITING);
            assertThat(updatedRoom.getCurrentParticipants()).isEqualTo(3);
        }
    }
}
