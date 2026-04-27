package ru.linskay.ymv;

import com.microsoft.playwright.Page;

public class PlayerController {
    private final Page page;

    public PlayerController(Page page) {
        this.page = page;
    }

    public PlayerState getState() {
        try {
            return page.evaluate("""
                () => {
                  const pickText = (...selectors) => selectors.map(s => document.querySelector(s)?.textContent?.trim()).find(Boolean) || '';
                  const pickAttr = (attr, ...selectors) => selectors.map(s => document.querySelector(s)?.getAttribute(attr)).find(Boolean) || '';
                  const title = pickText('[data-test-id=track-title]', '.track__title', '.player-controls__track-title', '[class*=TrackTitle]');
                  const artist = pickText('[data-test-id=track-artist]', '.track__artists', '.player-controls__artists', '[class*=Artist]');
                  const cover = pickAttr('src', '[data-test-id=track-cover] img', '.player-controls__track-cover img', '.track__cover img', 'img[src*="avatars.yandex"]', 'img[src*="music"]');
                  const audio = document.querySelector('audio');
                  const current = audio && Number.isFinite(audio.currentTime) ? audio.currentTime : 0;
                  const duration = audio && Number.isFinite(audio.duration) ? audio.duration : 0;
                  const volume = audio ? audio.volume : 0;
                  const playing = audio ? !audio.paused : false;
                  const liked = !!document.querySelector('[data-test-id=like-button][aria-pressed=true], .player-controls__btn_like.deco-button_checked, button[aria-label*=Нравится][aria-pressed=true]');
                  return { title, artist, cover, current, duration, progress: duration ? current / duration : 0, volume, playing, liked };
                }
            """);
        } catch (Exception e) {
            return new PlayerState("","","",0,0,0,0,false,false);
        }
    }
}
