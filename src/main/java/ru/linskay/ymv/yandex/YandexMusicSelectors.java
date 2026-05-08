package ru.linskay.ymv.yandex;

import java.util.List;

public final class YandexMusicSelectors {
    public static final List<String> PLAY_BUTTON = List.of(
            "[data-test-id='play-button']",
            ".player-controls__btn_play",
            "button[aria-label*='Воспроизвести']",
            "button[aria-label*='Пауза']"
    );

    public static final List<String> NEXT_BUTTON = List.of(
            "[data-test-id='next-button']",
            ".player-controls__btn_next",
            "button[aria-label*='Следующий']"
    );

    public static final List<String> PREV_BUTTON = List.of(
            "[data-test-id='prev-button']",
            ".player-controls__btn_prev",
            "button[aria-label*='Предыдущий']"
    );

    public static final List<String> LIKE_BUTTON = List.of(
            "[data-test-id='like-button']",
            ".player-controls__btn_like",
            "button[aria-label*='Нравится']"
    );

    public static final List<String> DISLIKE_BUTTON = List.of(
            "[data-test-id='dislike-button']",
            "button[aria-label*='Не нравится']"
    );

    public static final String AUDIO_ELEMENT = "audio";
}
