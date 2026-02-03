package com.benny.board_mate.room.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RoomCreateRequest {

    @NotNull(message = "게임 ID는 필수입니다")
    private Long gameId;

    @NotBlank(message = "지역은 필수입니다")
    private String region;

    private String cafeName;

    @NotNull(message = "게임 일시는 필수입니다")
    @Future(message = "게임 일시는 미래여야 합니다")
    private LocalDateTime gameDate;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다")
    private int maxParticipants;

    private String description;
}