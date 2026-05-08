package ru.linskay.ymv.web;

import io.javalin.websocket.WsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linskay.ymv.dto.PlayerStateDto;
import ru.linskay.ymv.yandex.YandexMusicController;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WsStateBroadcaster {
    private static final Logger logger = LoggerFactory.getLogger(WsStateBroadcaster.class);
    private final Set<WsContext> clients = ConcurrentHashMap.newKeySet();
    private final YandexMusicController controller;
    private Thread broadcastThread;

    public WsStateBroadcaster(YandexMusicController controller) {
        this.controller = controller;
    }

    public void addClient(WsContext ctx) {
        clients.add(ctx);
    }

    public void removeClient(WsContext ctx) {
        clients.remove(ctx);
    }

    public void start() {
        broadcastThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    PlayerStateDto state = controller.getState();
                    clients.forEach(client -> {
                        try {
                            client.send(state);
                        } catch (Exception e) {
                            clients.remove(client);
                        }
                    });
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Error in broadcast thread", e);
                }
            }
        }, "ws-broadcaster");
        broadcastThread.setDaemon(true);
        broadcastThread.start();
    }

    public void stop() {
        if (broadcastThread != null) {
            broadcastThread.interrupt();
        }
    }
}
