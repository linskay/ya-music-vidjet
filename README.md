# Ya Music Vidjet

Мини-виджет для Яндекс Музыки на Windows: Java backend + Svelte frontend + Playwright control.

## Концепт

Приложение работает как киберпанк overlay-плеер поверх рабочего стола. Яндекс Музыка управляется в фоне через Playwright, а пользователь видит только лёгкий неоновый виджет.

## Архитектура

```text
Svelte/Vite frontend
  ↓ HTTP API
Java 21 + Maven + Javalin backend
  ↓ Playwright
Yandex Music web player
```

## Основные режимы виджетов

- **Full HUD** — большой плеер как на концепте: обложка, трек, прогресс, громкость, prev/play/next/like/dislike, источник.
- **Slim Bar** — длинная тонкая панель с обложкой, названием, кнопками и громкостью.
- **Compact Controls** — маленькая панель только с основными кнопками.
- **Floating Orb** — круглая кнопка/шар, раскрывающая управление по клику или hover.
- **Source Menu** — панель выбора: Моя волна, плейлисты, новинки, качество.

## Settings-first сценарий

При первом запуске открывается окно настроек:

- автозапуск вместе с Windows;
- выбор активного виджета галочками;
- режим закрепления:
  - всегда поверх окон;
  - прикрепить к рабочему столу;
  - свободное плавание;
- lock position: перетащил куда нужно и заблокировал;
- запуск в фоне без кнопки/ярлыка на панели задач;
- открыть/закрыть виджет без остановки музыки;
- выбор стартового источника: по умолчанию `Моя волна`;
- включить/отключить анимации;
- сила glow/blur;
- горячие клавиши;
- диагностика Playwright-сессии и авторизации Яндекса.

## Управление

Backend API:

```text
GET /api/play
GET /api/prev
GET /api/next
GET /api/like
GET /api/dislike
GET /api/wave
GET /api/volume/{value}
```

## Сборка

Backend:

```bash
mvn clean package
java -jar target/ya-music-vidjet-0.1.0-SNAPSHOT.jar
```

Frontend:

```bash
cd frontend
npm install
npm run build
```

## Roadmap

### v0.1 — working prototype

- [x] Java 21 + Maven backend.
- [x] Javalin HTTP API.
- [x] Playwright управление Яндекс Музыкой.
- [x] play / prev / next / like / dislike / wave / volume.
- [x] headless/background browser mode.
- [x] DOM cleanup для уменьшения нагрузки.
- [x] `.gitignore`.
- [x] Svelte/Vite frontend scaffold.

### v0.2 — cyberpunk widget pack

- [ ] Full HUD виджет как на концепте.
- [ ] Slim Bar виджет.
- [ ] Compact Controls виджет.
- [ ] Floating Orb с раскрывающейся анимацией.
- [ ] Source Menu.
- [ ] SVG/clip-path неоновые контурные кнопки.
- [ ] Cyan/magenta glow animations.
- [ ] Drag & Snap.
- [ ] Lock position.

### v0.3 — settings app

- [ ] Первичный wizard настроек при установке.
- [ ] Автозапуск Windows.
- [ ] Выбор виджетов галочками.
- [ ] Always-on-top / desktop-pin / floating режимы.
- [ ] Hide settings window while widget keeps playing.
- [ ] Tray menu.
- [ ] Сохранение настроек в локальный config.

### v0.4 — packaging

- [ ] Fat jar.
- [ ] Windows `.exe` launcher.
- [ ] Installer.
- [ ] Автообновление позже.

## Важное ограничение

Селекторы Яндекс Музыки могут меняться. Поэтому управление делается через fallback-селекторы и отдельный слой `PlayerController`, чтобы быстро чинить изменения DOM.
