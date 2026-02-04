package com.benny.board_mate.sommelier.dto;

public record SommelierResponse(
    String type,
    String content,
    boolean completed
) {
    public static SommelierResponse text(String content) {
        return new SommelierResponse("text", content, false);
    }

    public static SommelierResponse done() {
        return new SommelierResponse("done", null, true);
    }

    public static SommelierResponse error(String message) {
        return new SommelierResponse("error", message, true);
    }
}
