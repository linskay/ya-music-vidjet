package ru.linskay.ymv.web;

import io.javalin.Javalin;
import ru.linskay.ymv.bootstrap.ShutdownManager;
import ru.linskay.ymv.config.AppConfig;
import ru.linskay.ymv.config.ConfigService;
import ru.linskay.ymv.diagnostics.DiagnosticsService;
import ru.linskay.ymv.yandex.YandexMusicBrowser;
import ru.linskay.ymv.yandex.YandexMusicController;

import java.util.Map;

public class ApiRoutes {
    public static void register(Javalin app, ConfigService configService, YandexMusicController controller, YandexMusicBrowser browser) {
        DiagnosticsService diagnosticsService = new DiagnosticsService(controller);

        // Health
        app.get("/api/health", ctx -> ctx.json(diagnosticsService.getHealth()));

        // Config
        app.get("/api/config", ctx -> ctx.json(configService.get()));
        app.post("/api/config", ctx -> {
            AppConfig cfg = ctx.bodyAsClass(AppConfig.class);
            configService.save(cfg);
            ctx.json(cfg);
        });

        // Player State
        app.get("/api/player/state", ctx -> ctx.json(controller.getState()));

        // Player Commands
        app.post("/api/player/play-pause", ctx -> { controller.playPause(); ctx.json(Map.of("status", "ok")); });
        app.post("/api/player/next", ctx -> { controller.next(); ctx.json(Map.of("status", "ok")); });
        app.post("/api/player/previous", ctx -> { controller.previous(); ctx.json(Map.of("status", "ok")); });
        app.post("/api/player/like", ctx -> { controller.like(); ctx.json(Map.of("status", "ok")); });
        app.post("/api/player/dislike", ctx -> { controller.dislike(); ctx.json(Map.of("status", "ok")); });
        app.post("/api/player/wave", ctx -> { controller.startWave(); ctx.json(Map.of("status", "ok")); });

        app.post("/api/player/volume", ctx -> {
            Map<String, Double> body = ctx.bodyAsClass(Map.class);
            controller.setVolume(body.get("value"));
            ctx.json(Map.of("status", "ok"));
        });

        app.post("/api/player/seek", ctx -> {
            Map<String, Integer> body = ctx.bodyAsClass(Map.class);
            controller.seek(body.get("seconds"));
            ctx.json(Map.of("status", "ok"));
        });

        // Deprecated endpoints for backward compatibility
        app.get("/api/play", ctx -> { controller.playPause(); ctx.json(Map.of("status", "ok")); });
        app.get("/api/next", ctx -> { controller.next(); ctx.json(Map.of("status", "ok")); });
        app.get("/api/prev", ctx -> { controller.previous(); ctx.json(Map.of("status", "ok")); });
        app.get("/api/like", ctx -> { controller.like(); ctx.json(Map.of("status", "ok")); });
        app.get("/api/dislike", ctx -> { controller.dislike(); ctx.json(Map.of("status", "ok")); });
        app.get("/api/wave", ctx -> { controller.startWave(); ctx.json(Map.of("status", "ok")); });
        app.get("/api/volume/{value}", ctx -> {
            controller.setVolume(Double.parseDouble(ctx.pathParam("value")));
            ctx.json(Map.of("status", "ok"));
        });

        // System / Auth
        app.get("/api/login", ctx -> { controller.startWave(); ctx.json(Map.of("status", "ok")); });
        app.get("/api/session/reset", ctx -> { browser.resetSession(); ctx.json(Map.of("status", "ok")); });
        app.post("/api/system/shutdown", ctx -> {
            ctx.json(Map.of("status", "ok"));
            new Thread(ShutdownManager::shutdown).start();
        });
    }
}
