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
                  const title = document.querySelector('[data-test-id=track-title]')?.textContent || '';
                  const artist = document.querySelector('[data-test-id=track-artist]')?.textContent || '';
                  const cover = document.querySelector('img')?.src || '';
                  const audio = document.querySelector('audio');
                  return {
                    title,
                    artist,
                    cover,
                    current: audio ? Math.floor(audio.currentTime) : 0,
                    duration: audio ? Math.floor(audio.duration) : 0,
                    progress: audio && audio.duration ? audio.currentTime/audio.duration : 0,
                    volume: audio ? audio.volume : 0,
                    playing: audio ? !audio.paused : false,
                    liked: !!document.querySelector('[data-test-id=like-button][aria-pressed=true]')
                  };
                }
            """);
        } catch (Exception e) {
            return new PlayerState("","","","0","0",0,0,false,false);
        }
    }
}
