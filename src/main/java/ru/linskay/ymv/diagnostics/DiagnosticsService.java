package ru.linskay.ymv.diagnostics;

import ru.linskay.ymv.yandex.YandexMusicController;
import ru.linskay.ymv.dto.PlayerStateDto;

public class DiagnosticsService {
    private final YandexMusicController controller;

    public DiagnosticsService(YandexMusicController controller) {
        this.controller = controller;
    }

    public HealthStatus getHealth() {
        PlayerStateDto state = controller.getState();
        return HealthStatus.ok(state.authorized(), state.audioDetected());
    }
}
