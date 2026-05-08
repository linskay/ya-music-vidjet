package ru.linskay.ymv.yandex;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linskay.ymv.config.ConfigPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class YandexMusicBrowser {
    private static final Logger logger = LoggerFactory.getLogger(YandexMusicBrowser.class);

    private Playwright playwright;
    private BrowserContext browserContext;
    private Page page;

    public void init() {
        logger.info("Starting Playwright browser...");
        playwright = Playwright.create();
        Path profileDir = ConfigPaths.getBrowserProfileDir();
        try {
            Files.createDirectories(profileDir);
        } catch (IOException e) {
            logger.error("Failed to create browser profile directory", e);
        }

        browserContext = playwright.chromium().launchPersistentContext(profileDir, new BrowserType.LaunchPersistentContextOptions()
                .setHeadless(true)
                .setViewportSize(1280, 720)
                .setArgs(List.of(
                        "--autoplay-policy=no-user-gesture-required",
                        "--disable-gpu",
                        "--disable-background-timer-throttling",
                        "--disable-renderer-backgrounding",
                        "--no-first-run",
                        "--no-default-browser-check"
                )));

        page = browserContext.pages().isEmpty() ? browserContext.newPage() : browserContext.pages().get(0);
        page.navigate("https://music.yandex.ru/home");
        injectCleanupScript();
        logger.info("Yandex Music page loaded.");
    }

    private void injectCleanupScript() {
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

    public Page getPage() {
        return page;
    }

    public void close() {
        logger.info("Closing browser...");
        if (browserContext != null) browserContext.close();
        if (playwright != null) playwright.close();
    }

    public void resetSession() {
        close();
        try {
            deleteRecursively(ConfigPaths.getBrowserProfileDir());
        } catch (IOException e) {
            logger.error("Failed to delete browser profile", e);
        }
        init();
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var stream = Files.walk(path)) {
            stream.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        }
    }
}
