package ru.linskay.ymv.yandex;

import com.microsoft.playwright.Page;
import ru.linskay.ymv.dto.PlayerStateDto;

import java.util.Map;

public class YandexMusicStateReader {
    private final Page page;

    public YandexMusicStateReader(Page page) {
        this.page = page;
    }

    @SuppressWarnings("unchecked")
    public PlayerStateDto read() {
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

                  const liked = document.querySelector('[data-test-id="like-button"]') ?.classList.contains('button_checked') || false;

                  return {
                    title,
                    artist,
                    cover,
                    current,
                    duration,
                    progress: duration ? current / duration : 0,
                    volume,
                    playing,
                    liked,
                    audioDetected: !!audio,
                    authorized: !document.querySelector('.d-header__login-button')
                  };
                }
            """);

            Map<String, Object> map = (Map<String, Object>) result;

            return new PlayerStateDto(
                    (String) map.getOrDefault("title", ""),
                    (String) map.getOrDefault("artist", ""),
                    (String) map.getOrDefault("cover", ""),
                    ((Number) map.getOrDefault("current", 0)).doubleValue(),
                    ((Number) map.getOrDefault("duration", 0)).doubleValue(),
                    ((Number) map.getOrDefault("progress", 0)).doubleValue(),
                    ((Number) map.getOrDefault("volume", 0)).doubleValue(),
                    (Boolean) map.getOrDefault("playing", false),
                    (Boolean) map.getOrDefault("liked", false),
                    (Boolean) map.getOrDefault("audioDetected", false),
                    (Boolean) map.getOrDefault("authorized", true)
            );

        } catch (Exception e) {
            return PlayerStateDto.empty();
        }
    }
}
