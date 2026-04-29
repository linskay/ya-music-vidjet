<p align="center">
  <img src="docs/assets/logo.svg" alt="YA Music Widget" width="420" />
</p>

<h1 align="center">YA Music Widget</h1>

<p align="center">
  Лёгкий киберпанк-виджет для управления Яндекс Музыкой на Windows.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Windows-only-35f3ff?style=for-the-badge" alt="Windows" />
  <img src="https://img.shields.io/badge/Java-21-ff5d68?style=for-the-badge" alt="Java 21" />
  <img src="https://img.shields.io/badge/Svelte-Tauri-35f3ff?style=for-the-badge" alt="Svelte + Tauri" />
</p>

<p align="center">
  <img src="docs/assets/social-preview.png" alt="preview" width="100%" />
</p>

<img src="https://raw.githubusercontent.com/CDPR-Modding-Documentation/Cyberpunk-Modding-Docs/main/docs/assets/images/separator.svg" alt="separator" width="100%" />

## Что это

YA Music Widget — desktop overlay для Яндекс Музыки. Приложение открывает Яндекс Музыку в скрытом Playwright-браузере, сохраняет авторизацию и даёт управление через отдельный лёгкий виджет.

Основная идея: музыка работает в фоне, а на рабочем столе остаётся только нужный интерфейс — HUD, Slim или Orb.

<img src="https://raw.githubusercontent.com/CDPR-Modding-Documentation/Cyberpunk-Modding-Docs/main/docs/assets/images/separator.svg" alt="separator" width="100%" />

## Возможности

<p>
  <img src="https://cdn-icons-png.flaticon.com/512/727/727245.png" width="22" alt="play" /> управление воспроизведением: play / pause / next / previous
</p>

<p>
  <img src="https://cdn-icons-png.flaticon.com/512/833/833472.png" width="22" alt="like" /> like / dislike для треков
</p>

<p>
  <img src="https://cdn-icons-png.flaticon.com/512/727/727269.png" width="22" alt="wave" /> запуск «Моей волны»
</p>

<p>
  <img src="https://cdn-icons-png.flaticon.com/512/992/992651.png" width="22" alt="settings" /> настройки на русском языке
</p>

<p>
  <img src="https://cdn-icons-png.flaticon.com/512/1828/1828911.png" width="22" alt="tray" /> работа в фоне и закрытие в трей
</p>

<p>
  <img src="https://cdn-icons-png.flaticon.com/512/10435/10435152.png" width="22" alt="performance" /> адаптивные анимации и безопасный режим производительности
</p>

<img src="https://raw.githubusercontent.com/CDPR-Modding-Documentation/Cyberpunk-Modding-Docs/main/docs/assets/images/separator.svg" alt="separator" width="100%" />

## Виджеты

| Режим | Описание |
|---|---|
| HUD | основной киберпанк-интерфейс с обложкой, прогрессом, кнопками и реактивными эффектами |
| Slim | компактная панель для постоянного использования |
| Orb | минимальный режим, когда нужен только индикатор и быстрый возврат к HUD |

В один момент отображается только один режим. Это снижает нагрузку и не захламляет рабочий стол.

<img src="https://raw.githubusercontent.com/CDPR-Modding-Documentation/Cyberpunk-Modding-Docs/main/docs/assets/images/separator.svg" alt="separator" width="100%" />

## Авторизация

При первом запуске пользователь авторизуется в Яндекс Музыке. Сессия сохраняется в профиле приложения:

```text
%APPDATA%/YA Music Widget/browser-profile
```

После этого повторный вход обычно не требуется: приложение использует сохранённые cookies и localStorage.

<img src="https://raw.githubusercontent.com/CDPR-Modding-Documentation/Cyberpunk-Modding-Docs/main/docs/assets/images/separator.svg" alt="separator" width="100%" />

## Настройки

В приложении есть киберпанк-меню настроек с русским интерфейсом и подсказками.

Доступные параметры:

```text
автозапуск Windows
выбор виджета: HUD / Slim / Orb
стартовый источник: Моя волна
поверх всех окон
закрепление позиции
закрытие в трей
запуск без окна
реактивные эффекты: Low / Normal / Aggressive
экспериментальный FFT-анализ: Off / Low / Normal / High
```

По умолчанию тяжёлые функции выключены. FFT-анализ включается только вручную.

<img src="https://raw.githubusercontent.com/CDPR-Modding-Documentation/Cyberpunk-Modding-Docs/main/docs/assets/images/separator.svg" alt="separator" width="100%" />

## Установка

Готовые сборки публикуются в GitHub Releases.

Installer создаёт приложение для текущего пользователя. В настройках приложения можно включить или выключить автозапуск Windows.

<img src="https://raw.githubusercontent.com/CDPR-Modding-Documentation/Cyberpunk-Modding-Docs/main/docs/assets/images/separator.svg" alt="separator" width="100%" />

## Сборка из исходников

Требования:

```text
Java 21
Node.js 20+
Rust stable
Maven
```

Команды:

```bash
mvn -B clean package
cd frontend
npm install
npm run build
cd ..
cd src-tauri
cargo tauri build
```

<img src="https://raw.githubusercontent.com/CDPR-Modding-Documentation/Cyberpunk-Modding-Docs/main/docs/assets/images/separator.svg" alt="separator" width="100%" />

## Стек

```text
Java + Javalin
Playwright
Svelte + Vite
Tauri
WebSocket
GitHub Actions
```

<img src="https://raw.githubusercontent.com/CDPR-Modding-Documentation/Cyberpunk-Modding-Docs/main/docs/assets/images/separator.svg" alt="separator" width="100%" />

## Статус

Проект находится в активной разработке. Основной фокус сейчас — стабильность авторизации, лёгкость интерфейса, корректная работа настроек и удобная Windows-сборка.
