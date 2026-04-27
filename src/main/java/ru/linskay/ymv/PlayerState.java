package ru.linskay.ymv;

public record PlayerState(
        String title,
        String artist,
        String cover,
        String current,
        String duration,
        double progress,
        double volume,
        boolean playing,
        boolean liked
) {}
