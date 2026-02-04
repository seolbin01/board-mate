package com.benny.board_mate.participant;

import com.benny.board_mate.common.response.ApiResponse;
import com.benny.board_mate.participant.dto.AttendanceCheckRequest;
import com.benny.board_mate.participant.dto.ParticipantResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Participant", description = "참가자 - 모임 참가/퇴장, 출석 체크")
@RestController
@RequestMapping("/api/rooms/{roomId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping
    public ResponseEntity<ApiResponse<ParticipantResponse>> joinRoom(
            Authentication authentication,
            @PathVariable Long roomId) {
        Long userId = (Long) authentication.getPrincipal();
        ParticipantResponse response = participantService.joinRoom(userId, roomId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            Authentication authentication,
            @PathVariable Long roomId) {
        Long userId = (Long) authentication.getPrincipal();
        participantService.leaveRoom(userId, roomId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParticipantResponse>>> getParticipants(
            @PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.ok(participantService.getParticipants(roomId)));
    }

    @PostMapping("/attendance")
    public ResponseEntity<ApiResponse<Void>> checkAttendance(
            Authentication authentication,
            @PathVariable Long roomId,
            @RequestBody AttendanceCheckRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        participantService.checkAttendance(userId, roomId, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}