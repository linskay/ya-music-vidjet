package ru.linskay.ymv.web;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linskay.ymv.config.ConfigService;
import ru.linskay.ymv.yandex.YandexMusicBrowser;
import ru.linskay.ymv.yandex.YandexMusicController;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);

    private final ConfigService configService;
    private final YandexMusicController controller;
    private final YandexMusicBrowser browser;
    private final WsStateBroadcaster broadcaster;
    private Javalin app;

    public WebServer(ConfigService configService, YandexMusicController controller, YandexMusicBrowser browser) {
        this.configService = configService;
        this.controller = controller;
        this.browser = browser;
        this.broadcaster = new WsStateBroadcaster(controller);
    }

    public void start(int port) {
        logger.info("Starting Web Server on port {}...", port);
        app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.staticFiles.add("/public");
            config.router.contextPath = "/";
        });

        ApiRoutes.register(app, configService, controller, browser);

        app.ws("/ws/state", ws -> {
            ws.onConnect(broadcaster::addClient);
            ws.onClose(broadcaster::removeClient);
            ws.onError(broadcaster::removeClient);
        });

        app.start(port);
        broadcaster.start();
    }

    public void stop() {
        logger.info("Stopping Web Server...");
        broadcaster.stop();
        if (app != null) {
            app.stop();
        }
    }
}
