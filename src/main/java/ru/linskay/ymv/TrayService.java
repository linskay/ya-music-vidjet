package ru.linskay.ymv;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TrayService {

    public static void init() {
        if (!SystemTray.isSupported()) return;

        SystemTray tray = SystemTray.getSystemTray();
        Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        PopupMenu menu = new PopupMenu();
        MenuItem open = new MenuItem("Open UI");
        MenuItem exit = new MenuItem("Exit");

        open.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI("http://localhost:7070"));
            } catch (Exception ignored) {}
        });

        exit.addActionListener(e -> System.exit(0));

        menu.add(open);
        menu.addSeparator();
        menu.add(exit);

        TrayIcon trayIcon = new TrayIcon(image, "YA Music Widget", menu);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (Exception ignored) {}
    }
}
