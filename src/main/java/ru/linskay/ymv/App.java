package ru.linskay.ymv;

import com.microsoft.playwright.*;
import io.javalin.Javalin;

public class App {

    static Playwright playwright;
    static Browser browser;
    static Page page;

    public static void main(String[] args) {
        initBrowser();

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7070);

        app.get("/api/play", ctx -> {
            page.click("[data-test-id='play-button']");
            ctx.result("ok");
        });

        app.get("/api/next", ctx -> {
            page.click("[data-test-id='next-button']");
            ctx.result("ok");
        });

        app.get("/api/like", ctx -> {
            page.click("[data-test-id='like-button']");
            ctx.result("ok");
        });

        app.get("/api/wave", ctx -> {
            page.navigate("https://music.yandex.ru/home");
            ctx.result("ok");
        });
    }

    private static void initBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false));
        BrowserContext context = browser.newContext();
        page = context.newPage();
        page.navigate("https://music.yandex.ru/home");
    }
}
