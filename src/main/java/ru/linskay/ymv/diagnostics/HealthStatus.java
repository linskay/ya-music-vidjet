package ru.linskay.ymv.diagnostics;

import ru.linskay.ymv.config.ConfigPaths;

import java.time.Instant;

public record HealthStatus(
        String status,
        String version,
        Instant timestamp,
        BrowserInfo browser,
        YandexInfo yandex
) {
    public record BrowserInfo(String status, String profileDir) {}
    public record YandexInfo(boolean authorized, boolean audioDetected) {}

    public static HealthStatus ok(boolean authorized, boolean audioDetected) {
        return new HealthStatus(
                "ok",
                "0.1.0",
                Instant.now(),
                new BrowserInfo("ready", ConfigPaths.getBrowserProfileDir().toString()),
                new YandexInfo(authorized, audioDetected)
        );
    }
}
