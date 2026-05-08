package ru.linskay.ymv.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppConfig(
        boolean autostart,
        String widget,
        String startSource,
        boolean alwaysOnTop,
        boolean desktopPin,
        boolean floating,
        boolean locked,
        boolean closeToTray,
        boolean startHidden,
        boolean reactiveEffects,
        String reactiveMode,
        boolean experimentalFftAnalyzer,
        String fftMode,
        boolean disableFftInBackground,
        String theme,
        boolean reducedMotion,
        Position lastPosition
) {
    public record Position(int x, int y) {}

    public static AppConfig defaults() {
        return new AppConfig(
                false,
                "hud",
                "wave",
                true,
                false,
                true,
                false,
                true,
                false,
                true,
                "normal",
                false,
                "low",
                true,
                "neon-cyber",
                false,
                new Position(120, 40)
        );
    }
}
