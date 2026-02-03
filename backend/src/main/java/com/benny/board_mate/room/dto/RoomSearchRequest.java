package com.benny.board_mate.room.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class RoomSearchRequest {
    private String region;
    private Long gameId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    private int page = 0;
    private int size = 10;
}
