package ru.linskay.ymv.yandex;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linskay.ymv.dto.PlayerStateDto;

import java.util.List;

public class YandexMusicController {
    private static final Logger logger = LoggerFactory.getLogger(YandexMusicController.class);

    private final YandexMusicBrowser browser;
    private Page page;
    private YandexMusicStateReader stateReader;

    public YandexMusicController(YandexMusicBrowser browser) {
        this.browser = browser;
    }

    public void init() {
        this.page = browser.getPage();
        this.stateReader = new YandexMusicStateReader(page);
    }

    public PlayerStateDto getState() {
        if (stateReader == null) return PlayerStateDto.empty();
        return stateReader.read();
    }

    public void playPause() {
        clickAny(YandexMusicSelectors.PLAY_BUTTON);
    }

    public void next() {
        clickAny(YandexMusicSelectors.NEXT_BUTTON);
    }

    public void previous() {
        clickAny(YandexMusicSelectors.PREV_BUTTON);
    }

    public void like() {
        clickAny(YandexMusicSelectors.LIKE_BUTTON);
    }

    public void dislike() {
        clickAny(YandexMusicSelectors.DISLIKE_BUTTON);
    }

    public void startWave() {
        page.navigate("https://music.yandex.ru/home");
    }

    public void setVolume(double value) {
        double val = Math.max(0, Math.min(1, value));
        page.evaluate("value => { const a = document.querySelector('audio'); if (a) a.volume = value; }", val);
    }

    public void seek(int seconds) {
        page.evaluate("value => { const a = document.querySelector('audio'); if (a) a.currentTime = value; }", seconds);
    }

    private void clickAny(List<String> selectors) {
        for (String selector : selectors) {
            Locator locator = page.locator(selector).first();
            if (locator.count() > 0) {
                locator.click(new Locator.ClickOptions().setTimeout(1500));
                logger.debug("Clicked selector: {}", selector);
                return;
            }
        }
        logger.warn("None of the selectors worked: {}", selectors);
        throw new IllegalStateException("Control not found");
    }
}
