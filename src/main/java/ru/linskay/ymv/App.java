package ru.linskay.ymv;

import com.microsoft.playwright.*;
import io.javalin.Javalin;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

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
        app.get("/api/stream", ctx -> {
            ctx.contentType("text/event-stream");
            ctx.header("Cache-Control", "no-cache");
            ctx.header("Connection", "keep-alive");

            AtomicBoolean active = new AtomicBoolean(true);
            ctx.req().getAsyncContext();
            ctx.future(() -> java.util.concurrent.CompletableFuture.runAsync(() -> {
                try (PrintWriter writer = ctx.res().getWriter()) {
                    while (active.get() && !Thread.currentThread().isInterrupted()) {
                        String json = io.javalin.json.JavalinJackson.defaultMapper().toJsonString(controller.getState(), PlayerState.class);
                        writer.write("event: state\n");
                        writer.write("data: " + json + "\n\n");
                        writer.flush();
                        Thread.sleep(500);
                    }
                } catch (Exception ignored) {
                    active.set(false);
                }
            }));
        });

        app.get("/api/play", ctx -> runCommand(ctx, () -> clickAny("[data-test-id='play-button']", ".player-controls__btn_play", "button[aria-label*='Воспроизвести']", "button[aria-label*='Пауза']")));
        app.get("/api/next", ctx -> runCommand(ctx, () -> clickAny("[data-test-id='next-button']", ".player-controls__btn_next", "button[aria-label*='Следующий']")));
        app.get("/api/prev", ctx -> runCommand(ctx, () -> clickAny("[data-test-id='prev-button']", ".player-controls__btn_prev", "button[aria-label*='Предыдущий']")));
        app.get("/api/like", ctx -> runCommand(ctx, () -> clickAny("[data-test-id='like-button']", ".player-controls__btn_like", "button[aria-label*='Нравится']")));
        app.get("/api/dislike", ctx -> runCommand(ctx, () -> clickAny("[data-test-id='dislike-button']", "button[aria-label*='Не нравится']")));
        app.get("/api/wave", ctx -> runCommand(ctx, () -> { page.navigate("https://music.yandex.ru/home"); cleanupYandexUi(); }));
        app.get("/api/volume/{value}", ctx -> runCommand(ctx, () -> {
            double value = Math.max(0, Math.min(1, Double.parseDouble(ctx.pathParam("value"))));
            page.evaluate("value => { const a = document.querySelector('audio'); if (a) a.volume = value; }", value);
        }));

        Runtime.getRuntime().addShutdownHook(new Thread(App::shutdown));
    }

    private static void initBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(java.util.List.of("--autoplay-policy=no-user-gesture-required", "--disable-gpu")));
        BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 720));
        page = context.newPage();
        page.navigate("https://music.yandex.ru/home");
        cleanupYandexUi();
    }

    private static void runCommand(io.javalin.http.Context ctx, Runnable command) {
        try { command.run(); ctx.json(java.util.Map.of("status", "ok")); }
        catch (Exception e) { ctx.status(500).json(java.util.Map.of("status", "error", "message", e.getMessage())); }
    }

    private static void clickAny(String... selectors) {
        for (String selector : selectors) {
            Locator locator = page.locator(selector).first();
            if (locator.count() > 0) { locator.click(new Locator.ClickOptions().setTimeout(1500)); return; }
        }
        throw new IllegalStateException("Control not found: " + String.join(", ", selectors));
    }

    private static void cleanupYandexUi() {
        page.evaluate("""
                () => {
                  const remove = () => document.querySelectorAll('header, aside, video, iframe, .sidebar, .bar-below, .page-root__top').forEach(e => e.remove());
                  remove();
                  document.documentElement.style.setProperty('scroll-behavior', 'auto');
                  const style = document.createElement('style');
                  style.textContent = '*, *::before, *::after { animation: none !important; transition: none !important; }';
                  document.head.appendChild(style);
                  new MutationObserver(remove).observe(document.body, { childList: true, subtree: true });
                }
                """);
    }

    private static void shutdown() {
        try { if (browser != null) browser.close(); } catch (Exception ignored) {}
        try { if (playwright != null) playwright.close(); } catch (Exception ignored) {}
    }
}
