package com.benny.board_mate.participant.dto;

import com.benny.board_mate.participant.AttendanceStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AttendanceCheckRequest {

    private List<AttendanceItem> attendances;

    @Getter
    @NoArgsConstructor
    public static class AttendanceItem {
        private Long userId;
        private AttendanceStatus status;  // ATTENDED or NO_SHOW
    }
}