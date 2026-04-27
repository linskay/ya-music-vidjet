package ru.linskay.ymv;

import com.microsoft.playwright.*;
import io.javalin.Javalin;

public class App {
    private static Playwright playwright;
    private static Browser browser;
    private static Page page;
    private static PlayerController controller;

    public static void main(String[] args) {
        initBrowser();
        controller = new PlayerController(page);

        Javalin app = Javalin.create(config -> config.staticFiles.add("/public"))
                .start(7070);

        app.get("/api/state", ctx -> ctx.json(controller.getState()));

        // existing endpoints ... (оставил как есть)
    }

    private static void initBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        BrowserContext context = browser.newContext();
        page = context.newPage();
        page.navigate("https://music.yandex.ru/home");
    }
}
