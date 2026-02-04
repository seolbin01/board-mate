package com.benny.board_mate.chat;

import com.benny.board_mate.chat.dto.ChatMessageRequest;
import com.benny.board_mate.chat.dto.ChatMessageResponse;
import com.benny.board_mate.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat", description = "채팅 - 모임방 실시간 메시지")
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * WebSocket 메시지 전송
     * 클라이언트 -> /app/chat/{roomId}
     * 서버 -> /topic/chat/{roomId} 로 브로드캐스트
     */
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        // WebSocket 세션에서 userId 추출
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            chatService.sendMessage(roomId, userId, request);
        }
    }

    /**
     * REST API - 채팅 내역 조회
     */
    @GetMapping("/api/rooms/{roomId}/chats")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory(
            @PathVariable Long roomId) {
        List<ChatMessageResponse> history = chatService.getChatHistory(roomId);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    /**
     * REST API - 메시지 전송 (WebSocket 사용 못할 때 폴백)
     */
    @PostMapping("/api/rooms/{roomId}/chats")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessageRest(
            Authentication authentication,
            @PathVariable Long roomId,
            @RequestBody ChatMessageRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        ChatMessageResponse response = chatService.sendMessage(roomId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
