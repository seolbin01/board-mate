package com.benny.board_mate.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(400, "잘못된 입력입니다"),
    UNAUTHORIZED(401, "인증이 필요합니다"),
    FORBIDDEN(403, "권한이 없습니다"),
    NOT_FOUND(404, "리소스를 찾을 수 없습니다"),
    INTERNAL_ERROR(500, "서버 내부 오류입니다"),

    // Auth
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다"),
    DUPLICATE_NICKNAME(409, "이미 사용 중인 닉네임입니다"),
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다"),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다"),

    // Room
    ROOM_NOT_FOUND(404, "방을 찾을 수 없습니다"),
    ROOM_FULL(409, "방이 가득 찼습니다"),
    ROOM_ALREADY_JOINED(409, "이미 참가한 방입니다"),
    ROOM_NOT_HOST(403, "방장만 가능한 작업입니다"),
    ROOM_NOT_WAITING(400, "대기 중인 방이 아닙니다"),
    ROOM_HOST_CANNOT_LEAVE(409, "방장은 나갈 수 없습니다"),

    // User
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다"),

    // Game
    GAME_NOT_FOUND(404, "게임을 찾을 수 없습니다"),

    // Participant
    PARTICIPANT_NOT_FOUND(404, "참가 정보를 찾을 수 없습니다"),

    // Review
    REVIEW_SELF_NOT_ALLOWED(400, "자기 자신에게 리뷰를 작성할 수 없습니다"),
    REVIEW_DUPLICATE(409, "이미 해당 방에서 이 유저에게 리뷰를 작성했습니다"),
    REVIEW_ROOM_NOT_CLOSED(400, "게임이 종료된 방에서만 리뷰를 작성할 수 있습니다"),
    REVIEW_NOT_PARTICIPANT(403, "해당 방의 참가자만 리뷰를 작성할 수 있습니다"),

    // Sommelier
    SOMMELIER_GEMINI_ERROR(502, "AI 서비스 연결에 실패했습니다"),
    SOMMELIER_RATE_LIMIT(429, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요"),
    SOMMELIER_MESSAGE_TOO_LONG(400, "메시지가 너무 깁니다 (최대 1000자)");

    private final int status;
    private final String message;
}