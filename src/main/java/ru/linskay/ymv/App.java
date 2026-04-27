package ru.linskay.ymv;

import com.microsoft.playwright.*;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class App {
    private static Playwright playwright;
    private static Browser browser;
    private static Page page;
    private static PlayerController controller;
    private static ConfigService configService;
    private static final Set<WsContext> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        initBrowser();
        controller = new PlayerController(page);
        configService = new ConfigService();

        Javalin app = Javalin.create(config -> config.staticFiles.add("/public"))
                .start(7070);

        app.get("/api/config", ctx -> ctx.json(configService.get()));
        app.post("/api/config", ctx -> {
            AppConfig cfg = ctx.bodyAsClass(AppConfig.class);
            configService.save(cfg);
            ctx.json(cfg);
        });

        app.get("/api/state", ctx -> ctx.json(controller.getState()));
        app.ws("/ws/state", ws -> {
            ws.onConnect(ctx -> clients.add(ctx));
            ws.onClose(ctx -> clients.remove(ctx));
            ws.onError(ctx -> clients.remove(ctx));
        });
        startStateBroadcaster();

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

    private static void startStateBroadcaster() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String json = io.javalin.json.JavalinJackson.defaultMapper().toJsonString(controller.getState(), PlayerState.class);
                    for (WsContext client : clients) {
                        try { client.send(json); } catch (Exception e) { clients.remove(client); }
                    }
                    Thread.sleep(400);
                } catch (Exception ignored) {}
            }
        }, "player-state-broadcaster");
        thread.setDaemon(true);
        thread.start();
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
