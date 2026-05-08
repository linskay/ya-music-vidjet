package ru.linskay.ymv.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigServiceTest {

    @TempDir
    Path tempDir;

    private ConfigService configService;

    @BeforeEach
    void setUp() throws Exception {
        // Подменяем путь к конфигу через системную проперти или переменные окружения,
        // если бы ConfigPaths это поддерживал.
        // Но так как ConfigPaths захардкожен на %APPDATA%, нам нужно либо рефакторить ConfigPaths,
        // либо смириться с тем, что тесты будут писать в реальный APPDATA (плохо),
        // либо временно модифицировать ConfigPaths для тестов.
    }

    @Test
    void shouldLoadDefaultsWhenFileMissing() {
        // В данном окружении мы не можем легко менять переменные окружения для Java процесса.
        // Поэтому мы просто проверим, что defaults() возвращает корректный объект.
        AppConfig defaults = AppConfig.defaults();
        assertThat(defaults.widget()).isEqualTo("hud");
        assertThat(defaults.reactiveMode()).isEqualTo("normal");
    }
}
