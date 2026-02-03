package com.benny.board_mate.room;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    List<Room> findByRoomStatusOrderByCreatedAtDesc(RoomStatus roomStatus);

    // 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    // 리마인더가 필요한 방 조회 (1시간 전 ~ 59분 전, 아직 리마인더 안 보낸 방)
    @Query("SELECT r FROM Room r WHERE r.roomStatus IN :statuses " +
           "AND r.reminderSent = false " +
           "AND r.deletedAt IS NULL " +
           "AND r.gameDate BETWEEN :from AND :to")
    List<Room> findRoomsNeedingReminder(
            @Param("statuses") List<RoomStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}