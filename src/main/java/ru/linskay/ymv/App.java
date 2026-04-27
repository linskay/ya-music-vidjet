package ru.linskay.ymv;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7070);

        app.get("/api/play", ctx -> ctx.result("play"));
        app.get("/api/next", ctx -> ctx.result("next"));
        app.get("/api/like", ctx -> ctx.result("like"));
    }
}
