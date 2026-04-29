package ru.linskay.ymv;

import java.nio.file.Path;

public class AutoStartService {
    private static final String RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String APP_NAME = "YAMusicWidget";

    public static void apply(boolean enabled) {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return;
        }

        try {
            if (enabled) {
                enable();
            } else {
                disable();
            }
        } catch (Exception e) {
            System.err.println("Failed to apply autostart: " + e.getMessage());
        }
    }

    private static void enable() throws Exception {
        String exePath = resolveExecutablePath();
        new ProcessBuilder(
                "reg", "add", RUN_KEY,
                "/v", APP_NAME,
                "/t", "REG_SZ",
                "/d", "\"" + exePath + "\" --hidden",
                "/f"
        ).inheritIO().start().waitFor();
    }

    private static void disable() throws Exception {
        new ProcessBuilder(
                "reg", "delete", RUN_KEY,
                "/v", APP_NAME,
                "/f"
        ).inheritIO().start().waitFor();
    }

    private static String resolveExecutablePath() {
        String command = System.getProperty("sun.java.command", "");
        if (command.endsWith(".exe")) {
            return command;
        }

        String appDir = System.getProperty("user.dir", ".");
        return Path.of(appDir, "YA Music Widget.exe").toAbsolutePath().toString();
    }
}
