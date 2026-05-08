package ru.linskay.ymv.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linskay.ymv.config.ConfigService;
import ru.linskay.ymv.web.WebServer;
import ru.linskay.ymv.yandex.YandexMusicBrowser;
import ru.linskay.ymv.yandex.YandexMusicController;

public class ApplicationBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationBootstrap.class);

    private final ConfigService configService;
    private final YandexMusicBrowser browser;
    private final YandexMusicController controller;
    private final WebServer webServer;

    public ApplicationBootstrap() {
        logger.info("Initializing YA Music Widget Backend...");

        this.configService = new ConfigService();
        this.browser = new YandexMusicBrowser();
        this.controller = new YandexMusicController(browser);
        this.webServer = new WebServer(configService, controller, browser);

        ShutdownManager.registerHook(browser::close);
        ShutdownManager.registerHook(webServer::stop);

        Runtime.getRuntime().addShutdownHook(new Thread(ShutdownManager::shutdown));
    }

    public void start() {
        browser.init();
        controller.init();
        webServer.start(7070);
        logger.info("Application started successfully.");
    }
}
