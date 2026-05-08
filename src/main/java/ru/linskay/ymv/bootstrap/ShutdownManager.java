package ru.linskay.ymv.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ShutdownManager {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownManager.class);
    private static final List<Runnable> hooks = new ArrayList<>();

    public static synchronized void registerHook(Runnable hook) {
        hooks.add(hook);
    }

    public static synchronized void shutdown() {
        logger.info("Initiating graceful shutdown...");
        for (Runnable hook : hooks) {
            try {
                hook.run();
            } catch (Exception e) {
                logger.error("Error during shutdown hook execution", e);
            }
        }
        logger.info("Shutdown complete.");
    }
}
