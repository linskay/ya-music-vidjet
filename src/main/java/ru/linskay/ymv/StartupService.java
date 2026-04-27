package ru.linskay.ymv;

import java.io.File;
import java.io.IOException;

public class StartupService {

    public static void enableAutostart() {
        try {
            String startup = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            File target = new File(startup, "YA-Music-Widget.bat");
            String path = new File(".").getAbsolutePath();
            String content = "start " + path + "\\yandex-mini-player.exe";
            java.nio.file.Files.writeString(target.toPath(), content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
