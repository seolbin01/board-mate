package com.benny.board_mate.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    // 최근 N개 메시지 조회
    List<ChatMessage> findTop50ByRoomIdOrderByCreatedAtDesc(Long roomId);
}
