package com.benny.board_mate.participant;

import com.benny.board_mate.game.BoardGame;
import com.benny.board_mate.game.GameRepository;
import com.benny.board_mate.room.Room;
import com.benny.board_mate.room.RoomRepository;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import com.benny.board_mate.trust.TrustScore;
import com.benny.board_mate.trust.TrustScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ParticipantConcurrencyTest {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ParticipantServiceOptimistic participantServiceOptimistic;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private TrustScoreRepository trustScoreRepository;

    private BoardGame testGame;
    private static final int MAX_PARTICIPANTS = 4;
    private static final int CONCURRENT_USERS = 100;

    @BeforeEach
    void setUp() {
        testGame = gameRepository.findAll().stream().findFirst()
                .orElseGet(() -> gameRepository.save(BoardGame.builder()
                        .title("테스트게임")
                        .minPlayers(2)
                        .maxPlayers(4)
                        .build()));
    }

    private Room createTestRoom(String uniqueId) {
        User host = createUserWithTrustScore("host_" + uniqueId + "@test.com", "방장_" + uniqueId);

        Room room = roomRepository.save(Room.builder()
                .host(host)
                .game(testGame)
                .region("서울")
                .gameDate(LocalDateTime.now().plusDays(1))
                .maxParticipants(MAX_PARTICIPANTS)
                .build());

        participantRepository.save(Participant.builder()
                .room(room)
                .user(host)
                .build());

        return room;
    }

    private User createUserWithTrustScore(String email, String nickname) {
        User user = userRepository.save(User.builder()
                .email(email)
                .password("password")
                .nickname(nickname)
                .role("USER")
                .build());

        trustScoreRepository.save(TrustScore.builder()
                .user(user)
                .score(100)
                .build());

        return user;
    }

    @Test
    @DisplayName("비관적 락: 100명 동시 참가 시 정원(4명) 초과 방지")
    void pessimisticLock_preventOverbooking() throws InterruptedException {
        String uniqueId = "pessimistic_" + System.currentTimeMillis();
        Room testRoom = createTestRoom(uniqueId);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            users.add(createUserWithTrustScore(
                "user" + i + "_" + uniqueId + "@test.com",
                "유저" + i + "_" + uniqueId));
        }

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (User user : users) {
            executor.submit(() -> {
                try {
                    participantService.joinRoom(user.getId(), testRoom.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();

        Room updatedRoom = roomRepository.findById(testRoom.getId()).orElseThrow();
        int participantCount = participantRepository.findByRoom(updatedRoom).size();

        System.out.println("=== 비관적 락 테스트 결과 ===");
        System.out.println("소요 시간: " + (endTime - startTime) + "ms");
        System.out.println("참가 성공: " + successCount.get());
        System.out.println("참가 실패: " + failCount.get());
        System.out.println("실제 참가자 수: " + participantCount);

        assertThat(participantCount).isEqualTo(MAX_PARTICIPANTS);
        assertThat(successCount.get()).isEqualTo(MAX_PARTICIPANTS - 1);
    }

    @Test
    @DisplayName("낙관적 락: 100명 동시 참가 시 정원 초과 가능성 (재시도로 완화)")
    void optimisticLock_withRetry() throws InterruptedException {
        String uniqueId = "optimistic_" + System.currentTimeMillis();
        Room testRoom = createTestRoom(uniqueId);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            users.add(createUserWithTrustScore(
                "user" + i + "_" + uniqueId + "@test.com",
                "유저" + i + "_" + uniqueId));
        }

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (User user : users) {
            executor.submit(() -> {
                try {
                    participantServiceOptimistic.joinRoomWithRetry(user.getId(), testRoom.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();

        Room updatedRoom = roomRepository.findById(testRoom.getId()).orElseThrow();
        int participantCount = participantRepository.findByRoom(updatedRoom).size();

        System.out.println("=== 낙관적 락 테스트 결과 ===");
        System.out.println("소요 시간: " + (endTime - startTime) + "ms");
        System.out.println("참가 성공: " + successCount.get());
        System.out.println("참가 실패: " + failCount.get());
        System.out.println("실제 참가자 수: " + participantCount);

        // 낙관적 락은 정원 초과가 발생할 수 있음!
        // 재시도 로직이 있어도 race condition 완벽 방지 어려움
        System.out.println("정원 초과 여부: " + (participantCount > MAX_PARTICIPANTS));
    }
}