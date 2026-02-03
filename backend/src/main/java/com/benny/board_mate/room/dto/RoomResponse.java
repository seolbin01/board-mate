package com.benny.board_mate.room.dto;

import com.benny.board_mate.room.Room;
import com.benny.board_mate.room.RoomStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomResponse {

    private Long id;
    private String hostNickname;
    private String gameTitle;
    private String region;
    private String cafeName;
    private LocalDateTime gameDate;
    private int maxParticipants;
    private int currentParticipants;
    private RoomStatus roomStatus;
    private String description;
    private LocalDateTime createdAt;

    public static RoomResponse from(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .hostNickname(room.getHost().getNickname())
                .gameTitle(room.getGame().getTitle())
                .region(room.getRegion())
                .cafeName(room.getCafeName())
                .gameDate(room.getGameDate())
                .maxParticipants(room.getMaxParticipants())
                .currentParticipants(room.getCurrentParticipants())
                .roomStatus(room.getRoomStatus())
                .description(room.getDescription())
                .createdAt(room.getCreatedAt())
                .build();
    }
}