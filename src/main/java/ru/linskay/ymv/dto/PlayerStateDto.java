package ru.linskay.ymv.dto;

public record PlayerStateDto(
        String title,
        String artist,
        String cover,
        double current,
        double duration,
        double progress,
        double volume,
        boolean playing,
        boolean liked,
        boolean audioDetected,
        boolean authorized
) {
    public static PlayerStateDto empty() {
        return new PlayerStateDto("", "", "", 0, 0, 0, 0, false, false, false, false);
    }
}
