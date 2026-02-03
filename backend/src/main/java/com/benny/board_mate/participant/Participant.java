package com.benny.board_mate.participant;

import com.benny.board_mate.room.Room;
import com.benny.board_mate.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus attendanceStatus;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime cancelledAt;

    @Builder
    public Participant(Room room, User user) {
        this.room = room;
        this.user = user;
        this.attendanceStatus = AttendanceStatus.PENDING;
        this.joinedAt = LocalDateTime.now();
    }

    public void markAttended() {
        this.attendanceStatus = AttendanceStatus.ATTENDED;
    }

    public void markNoShow() {
        this.attendanceStatus = AttendanceStatus.NO_SHOW;
    }

    public void cancel() {
        this.attendanceStatus = AttendanceStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
    
    public void updateAttendanceStatus(AttendanceStatus status) {
        this.attendanceStatus = status;
    }
}