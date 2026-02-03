package com.benny.board_mate.participant;

import com.benny.board_mate.room.Room;
import com.benny.board_mate.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    Optional<Participant> findByRoomIdAndUserId(Long roomId, Long userId);

    List<Participant> findByRoomId(Long roomId);

    List<Participant> findByUserId(Long userId);

    boolean existsByRoomAndUser(Room room, User user);

    Optional<Participant> findByRoomAndUser(Room room, User user);

    List<Participant> findByRoom(Room room);
}