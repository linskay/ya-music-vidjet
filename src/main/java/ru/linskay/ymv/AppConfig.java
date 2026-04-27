package ru.linskay.ymv;

public record AppConfig(
        boolean autostart,
        String widget,
        String startSource,
        String windowMode,
        boolean alwaysOnTop,
        boolean desktopPin,
        boolean floating,
        boolean locked,
        boolean closeToTray,
        boolean hideUi,
        boolean startHidden
) {
    public static AppConfig defaults() {
        return new AppConfig(true, "hud", "wave", "floating", true, false, true, false, true, false, false);
    }
}
