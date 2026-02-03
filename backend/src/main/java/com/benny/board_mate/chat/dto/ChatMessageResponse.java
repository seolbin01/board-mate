package com.benny.board_mate.chat.dto;

import com.benny.board_mate.chat.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {

    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoom().getId())
                .senderId(message.getSender().getId())
                .senderNickname(message.getSender().getNickname())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
