package ru.linskay.ymv.yandex;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class YandexMusicSelectorsTest {

    @Test
    void shouldHaveRequiredSelectors() {
        assertThat(YandexMusicSelectors.PLAY_BUTTON).isNotEmpty();
        assertThat(YandexMusicSelectors.NEXT_BUTTON).isNotEmpty();
        assertThat(YandexMusicSelectors.PREV_BUTTON).isNotEmpty();
    }
}
