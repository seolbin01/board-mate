package com.benny.board_mate.chat;

import com.benny.board_mate.chat.dto.ChatMessageRequest;
import com.benny.board_mate.chat.dto.ChatMessageResponse;
import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.participant.ParticipantRepository;
import com.benny.board_mate.room.Room;
import com.benny.board_mate.room.RoomRepository;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponse sendMessage(Long roomId, Long senderId, ChatMessageRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 방 참가자만 채팅 가능
        if (!participantRepository.existsByRoomAndUser(room, sender)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_PARTICIPANT);
        }

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(request.getContent())
                .build();

        chatMessageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.from(message);

        // WebSocket으로 실시간 전송
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);

        return response;
    }

    public List<ChatMessageResponse> getChatHistory(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // 최근 50개 메시지를 역순으로 가져온 뒤 시간순 정렬
        return chatMessageRepository.findTop50ByRoomIdOrderByCreatedAtDesc(roomId)
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(ChatMessageResponse::from)
                .toList();
    }
}
