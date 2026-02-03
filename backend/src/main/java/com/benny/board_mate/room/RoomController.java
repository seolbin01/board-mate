package com.benny.board_mate.room;

import com.benny.board_mate.common.response.ApiResponse;
import com.benny.board_mate.room.dto.RoomCreateRequest;
import com.benny.board_mate.room.dto.RoomResponse;
import com.benny.board_mate.room.dto.RoomSearchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            Authentication authentication,
            @Valid @RequestBody RoomCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        RoomResponse response = roomService.createRoom(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getWaitingRooms(
            @ModelAttribute RoomSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.searchRooms(request)));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.getRoom(roomId)));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            Authentication authentication,
            @PathVariable Long roomId) {
        Long userId = (Long) authentication.getPrincipal();
        roomService.deleteRoom(userId, roomId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}