package ru.linskay.ymv.player;

public record PlayerState(
        String title,
        String artist,
        String coverUrl,
        String currentTime,
        String duration,
        boolean playing,
        int volume
) {}
