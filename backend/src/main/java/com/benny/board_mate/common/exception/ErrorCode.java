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
    PARTICIPANT_NOT_FOUND(404, "참가 정보를 찾을 수 없습니다");

    private final int status;
    private final String message;
}