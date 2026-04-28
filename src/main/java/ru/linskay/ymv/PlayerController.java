package ru.linskay.ymv;

import com.microsoft.playwright.Page;
import java.util.Map;

public class PlayerController {
    private final Page page;

    public PlayerController(Page page) {
        this.page = page;
    }

    @SuppressWarnings("unchecked")
    public PlayerState getState() {
        try {
            Object result = page.evaluate("""
                () => {
                  const pickText = (...selectors) => selectors.map(s => document.querySelector(s)?.textContent?.trim()).find(Boolean) || '';
                  const pickAttr = (attr, ...selectors) => selectors.map(s => document.querySelector(s)?.getAttribute(attr)).find(Boolean) || '';
                  const title = pickText('[data-test-id=track-title]', '.track__title', '.player-controls__track-title', '[class*=TrackTitle]');
                  const artist = pickText('[data-test-id=track-artist]', '.track__artists', '.player-controls__artists', '[class*=Artist]');
                  const cover = pickAttr('src', '[data-test-id=track-cover] img', '.player-controls__track-cover img', '.track__cover img');
                  const audio = document.querySelector('audio');
                  const current = audio && Number.isFinite(audio.currentTime) ? audio.currentTime : 0;
                  const duration = audio && Number.isFinite(audio.duration) ? audio.duration : 0;
                  const volume = audio ? audio.volume : 0;
                  const playing = audio ? !audio.paused : false;
                  const liked = false;
                  return { title, artist, cover, current, duration, progress: duration ? current / duration : 0, volume, playing, liked };
                }
            """);

            Map<String, Object> map = (Map<String, Object>) result;

            return new PlayerState(
                    (String) map.getOrDefault("title", ""),
                    (String) map.getOrDefault("artist", ""),
                    (String) map.getOrDefault("cover", ""),
                    String.valueOf(map.getOrDefault("current", 0)),
                    String.valueOf(map.getOrDefault("duration", 0)),
                    ((Number) map.getOrDefault("progress", 0)).doubleValue(),
                    ((Number) map.getOrDefault("volume", 0)).doubleValue(),
                    (Boolean) map.getOrDefault("playing", false),
                    (Boolean) map.getOrDefault("liked", false)
            );

        } catch (Exception e) {
            return new PlayerState("", "", "", "0", "0", 0, 0, false, false);
        }
    }
}
