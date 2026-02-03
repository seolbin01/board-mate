package com.benny.board_mate.room;

import com.benny.board_mate.common.entity.BaseEntity;
import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.game.BoardGame;
import com.benny.board_mate.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private BoardGame game;

    @Column(nullable = false, length = 50)
    private String region;

    @Column(length = 100)
    private String cafeName;

    @Column(nullable = false)
    private LocalDateTime gameDate;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private int currentParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RoomStatus roomStatus;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Version
    private Long version;

    @Builder
    public Room(User host, BoardGame game, String region, String cafeName,
                LocalDateTime gameDate, int maxParticipants, String description) {
        this.host = host;
        this.game = game;
        this.region = region;
        this.cafeName = cafeName;
        this.gameDate = gameDate;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = 1;
        this.roomStatus = RoomStatus.WAITING;
        this.description = description;
    }

    public void addParticipant() {
        if (this.currentParticipants >= this.maxParticipants) {
            throw new BusinessException(ErrorCode.ROOM_FULL);
        }
        this.currentParticipants++;
        if (this.currentParticipants == this.maxParticipants) {
            this.roomStatus = RoomStatus.FULL;
        }
    }

    public void removeParticipant() {
        this.currentParticipants--;
        if (this.roomStatus == RoomStatus.FULL) {
            this.roomStatus = RoomStatus.WAITING;
        }
    }

    public void startGame() {
        this.roomStatus = RoomStatus.PLAYING;
    }

    public void closeRoom() {
        this.roomStatus = RoomStatus.CLOSED;
    }

    public boolean isHost(User user) {
        return this.host.getId().equals(user.getId());
    }
}