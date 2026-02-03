package com.benny.board_mate.participant.dto;

import com.benny.board_mate.participant.AttendanceStatus;
import com.benny.board_mate.participant.Participant;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ParticipantResponse {

    private Long id;
    private Long roomId;
    private Long userId;
    private String nickname;
    private AttendanceStatus attendanceStatus;
    private LocalDateTime joinedAt;

    public static ParticipantResponse from(Participant participant) {
        return ParticipantResponse.builder()
                .id(participant.getId())
                .roomId(participant.getRoom().getId())
                .userId(participant.getUser().getId())
                .nickname(participant.getUser().getNickname())
                .attendanceStatus(participant.getAttendanceStatus())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}