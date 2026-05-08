package ru.linskay.ymv.config;

import java.nio.file.Path;

public final class ConfigPaths {
    private static final String APP_DIR = "YA Music Widget";

    public static Path getAppDataDir() {
        String base = System.getenv("APPDATA");
        if (base == null || base.isBlank()) {
            base = System.getProperty("user.home", ".");
        }
        return Path.of(base, APP_DIR);
    }

    public static Path getConfigPath() {
        return getAppDataDir().resolve("config.json");
    }

    public static Path getBrowserProfileDir() {
        return getAppDataDir().resolve("browser-profile");
    }

    public static Path getLogsDir() {
        return getAppDataDir().resolve("logs");
    }
}
